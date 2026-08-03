package com.lifelink.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.accounting.entity.AccountBook;
import com.lifelink.accounting.entity.Transaction;
import com.lifelink.accounting.entity.TransactionCategory;
import com.lifelink.accounting.mapper.AccountBookMapper;
import com.lifelink.accounting.mapper.TransactionCategoryMapper;
import com.lifelink.accounting.mapper.TransactionMapper;
import com.lifelink.ai.dto.MemoryBuildResponse;
import com.lifelink.ai.entity.MemoryVector;
import com.lifelink.ai.mapper.MemoryVectorMapper;
import com.lifelink.anniversary.entity.Anniversary;
import com.lifelink.anniversary.mapper.AnniversaryMapper;
import com.lifelink.common.BusinessException;
import com.lifelink.daily.entity.DailyPost;
import com.lifelink.daily.entity.DailyPostComment;
import com.lifelink.daily.mapper.DailyPostCommentMapper;
import com.lifelink.daily.mapper.DailyPostMapper;
import com.lifelink.relationship.service.RelationshipPermissionService;
import com.lifelink.todo.entity.SpaceTodo;
import com.lifelink.todo.mapper.SpaceTodoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemoryBuildService {

    private static final int CHUNK_SIZE = 600;
    private static final int CHUNK_OVERLAP = 80;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy\u5e74M\u6708d\u65e5");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy\u5e74M\u6708d\u65e5 HH:mm");

    private final RelationshipPermissionService relationshipPermissionService;
    private final DailyPostMapper dailyPostMapper;
    private final DailyPostCommentMapper dailyPostCommentMapper;
    private final AnniversaryMapper anniversaryMapper;
    private final SpaceTodoMapper spaceTodoMapper;
    private final AccountBookMapper accountBookMapper;
    private final TransactionMapper transactionMapper;
    private final TransactionCategoryMapper transactionCategoryMapper;
    private final MemoryVectorMapper memoryVectorMapper;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper;

    @Transactional
    public MemoryBuildResponse build(Long spaceId, Long userId) {
        relationshipPermissionService.requireActiveRelationshipMember(spaceId, userId);
        List<MemorySource> sources = collectSources(spaceId);
        memoryVectorMapper.deleteByUserAndSpace(userId, spaceId);

        int chunkCount = 0;
        for (MemorySource source : sources) {
            for (String chunk : split(source.content())) {
                MemoryVector memory = new MemoryVector();
                memory.setUserId(userId);
                memory.setSpaceId(spaceId);
                memory.setSourceType(source.sourceType());
                memory.setSourceId(source.sourceId());
                memory.setContent(chunk);
                memory.setMetadata(toMetadata(source));
                memory.setCreatedTime(LocalDateTime.now());
                memoryVectorMapper.insertMemory(memory, toVectorLiteral(embeddingService.embed(chunk)));
                chunkCount++;
            }
        }
        return new MemoryBuildResponse(sources.size(), chunkCount);
    }

    private List<MemorySource> collectSources(Long spaceId) {
        List<MemorySource> sources = new ArrayList<MemorySource>();
        List<DailyPost> posts = dailyPostMapper.selectList(new LambdaQueryWrapper<DailyPost>()
                .eq(DailyPost::getRelationshipId, spaceId)
                .eq(DailyPost::getStatus, "ACTIVE")
                .orderByAsc(DailyPost::getCreatedAt));
        for (DailyPost post : posts) {
            sources.add(new MemorySource("MOMENT", post.getId(), toMomentContent(post)));
        }

        for (Anniversary anniversary : anniversaryMapper.selectList(new LambdaQueryWrapper<Anniversary>()
                .eq(Anniversary::getRelationshipId, spaceId)
                .eq(Anniversary::getStatus, "ACTIVE")
                .orderByAsc(Anniversary::getAnniversaryDate))) {
            sources.add(new MemorySource("EVENT", anniversary.getId(), toEventContent(anniversary)));
        }

        for (SpaceTodo todo : spaceTodoMapper.selectList(new LambdaQueryWrapper<SpaceTodo>()
                .eq(SpaceTodo::getRelationshipId, spaceId)
                .ne(SpaceTodo::getStatus, "DELETED")
                .orderByAsc(SpaceTodo::getCreatedAt))) {
            sources.add(new MemorySource("TODO", todo.getId(), toTodoContent(todo)));
        }

        sources.addAll(collectBills(spaceId));
        sources.addAll(collectComments(posts));
        return sources;
    }

    private List<MemorySource> collectBills(Long spaceId) {
        List<AccountBook> books = accountBookMapper.selectList(new LambdaQueryWrapper<AccountBook>()
                .eq(AccountBook::getRelationshipId, spaceId)
                .eq(AccountBook::getStatus, "ACTIVE"));
        if (books.isEmpty()) {
            return List.of();
        }
        List<Long> bookIds = books.stream().map(AccountBook::getId).toList();
        Map<Long, String> categoryNames = new HashMap<Long, String>();
        List<MemorySource> sources = new ArrayList<MemorySource>();
        for (Transaction transaction : transactionMapper.selectList(new LambdaQueryWrapper<Transaction>()
                .in(Transaction::getAccountBookId, bookIds)
                .eq(Transaction::getStatus, "ACTIVE")
                .orderByAsc(Transaction::getTransactionTime))) {
            String categoryName = categoryNames.computeIfAbsent(transaction.getCategoryId(), this::findCategoryName);
            sources.add(new MemorySource("BILL", transaction.getId(), toBillContent(transaction, categoryName)));
        }
        return sources;
    }

    private List<MemorySource> collectComments(List<DailyPost> posts) {
        if (posts.isEmpty()) {
            return List.of();
        }
        List<Long> postIds = posts.stream().map(DailyPost::getId).toList();
        List<MemorySource> sources = new ArrayList<MemorySource>();
        for (DailyPostComment comment : dailyPostCommentMapper.selectList(new LambdaQueryWrapper<DailyPostComment>()
                .in(DailyPostComment::getDailyPostId, postIds)
                .eq(DailyPostComment::getStatus, "ACTIVE")
                .orderByAsc(DailyPostComment::getCreatedAt))) {
            sources.add(new MemorySource("COMMENT", comment.getId(), toCommentContent(comment)));
        }
        return sources;
    }

    private String findCategoryName(Long categoryId) {
        if (categoryId == null) {
            return "\u672a\u5206\u7c7b";
        }
        TransactionCategory category = transactionCategoryMapper.selectById(categoryId);
        return category == null || !StringUtils.hasText(category.getName()) ? "\u672a\u5206\u7c7b" : category.getName();
    }

    private String toMomentContent(DailyPost post) {
        return dateTime(post.getCreatedAt()) + "\uff0c\u6211\u4eec\u8bb0\u5f55\u4e86\u4e00\u5219\u65e5\u5e38\uff1a" + post.getContent().trim()
                + (StringUtils.hasText(post.getMood()) ? "\u3002\u5f53\u65f6\u7684\u5fc3\u60c5\u662f" + post.getMood() : "") + "\u3002";
    }

    private String toEventContent(Anniversary anniversary) {
        return date(anniversary.getAnniversaryDate()) + "\uff0c\u91cd\u8981\u7eaa\u5ff5\u65e5'" + anniversary.getTitle().trim() + "'"
                + (StringUtils.hasText(anniversary.getDescription()) ? "\uff1a" + anniversary.getDescription().trim() : "\u3002");
    }

    private String toTodoContent(SpaceTodo todo) {
        LocalDateTime time = todo.getCompletedAt() != null ? todo.getCompletedAt() : (todo.getDueTime() != null ? todo.getDueTime() : todo.getCreatedAt());
        String status = "DONE".equals(todo.getStatus()) ? "\u5b8c\u6210\u4e86" : "\u8bb0\u5f55\u4e86\u5f85\u529e\u4efb\u52a1";
        return dateTime(time) + "\uff0c\u6211\u4eec" + status + "'" + todo.getTitle().trim() + "'"
                + (StringUtils.hasText(todo.getContent()) ? "\uff1a" + todo.getContent().trim() : "\u3002");
    }

    private String toBillContent(Transaction transaction, String categoryName) {
        String type = "INCOME".equals(transaction.getType()) ? "\u6536\u5165" : "\u652f\u51fa";
        return dateTime(transaction.getTransactionTime()) + "\uff0c\u6211\u4eec\u6709\u4e00\u7b14" + type + "\uff0c\u91d1\u989d" + transaction.getAmount()
                + "\u5143\uff0c\u5206\u7c7b\u4e3a" + categoryName + "\uff0c\u4e8b\u9879\u662f'" + transaction.getTitle().trim() + "'"
                + (StringUtils.hasText(transaction.getNote()) ? "\uff0c\u5907\u6ce8\uff1a" + transaction.getNote().trim() : "\u3002");
    }

    private String toCommentContent(DailyPostComment comment) {
        return dateTime(comment.getCreatedAt()) + "\uff0c\u6211\u4eec\u5728\u5173\u7cfb\u7a7a\u95f4\u7684\u65e5\u5e38\u8bb0\u5f55\u4e0b\u7559\u4e0b\u8bc4\u8bba\uff1a" + comment.getContent().trim() + "\u3002";
    }

    private List<String> split(String content) {
        String normalized = content == null ? "" : content.trim();
        if (!StringUtils.hasText(normalized)) {
            return List.of();
        }
        List<String> chunks = new ArrayList<String>();
        for (int start = 0; start < normalized.length();) {
            int end = Math.min(normalized.length(), start + CHUNK_SIZE);
            chunks.add(normalized.substring(start, end));
            if (end == normalized.length()) {
                break;
            }
            start = end - CHUNK_OVERLAP;
        }
        return chunks;
    }

    private String toMetadata(MemorySource source) {
        try {
            return objectMapper.writeValueAsString(Map.of("sourceType", source.sourceType(), "sourceId", source.sourceId()));
        } catch (JsonProcessingException exception) {
            throw new BusinessException(500, "Failed to create memory metadata");
        }
    }

    public static String toVectorLiteral(float[] vector) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(vector[i]);
        }
        return builder.append(']').toString();
    }

    private String date(LocalDate value) {
        return value == null ? "\u672a\u77e5\u65e5\u671f" : value.format(DATE_FORMATTER);
    }

    private String dateTime(LocalDateTime value) {
        return value == null ? "\u672a\u77e5\u65f6\u95f4" : value.format(DATE_TIME_FORMATTER);
    }

    private record MemorySource(String sourceType, Long sourceId, String content) {
    }
}
