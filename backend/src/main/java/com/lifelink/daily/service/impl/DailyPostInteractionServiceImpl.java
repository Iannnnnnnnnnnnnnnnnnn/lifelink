package com.lifelink.daily.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifelink.activity.service.SpaceActivityService;
import com.lifelink.common.BusinessException;
import com.lifelink.daily.dto.CommentDailyPostRequest;
import com.lifelink.daily.dto.DailyPostCommentResponse;
import com.lifelink.daily.dto.DailyPostInteractionResponse;
import com.lifelink.daily.entity.DailyPost;
import com.lifelink.daily.entity.DailyPostComment;
import com.lifelink.daily.entity.DailyPostLike;
import com.lifelink.daily.mapper.DailyPostCommentMapper;
import com.lifelink.daily.mapper.DailyPostLikeMapper;
import com.lifelink.daily.mapper.DailyPostMapper;
import com.lifelink.daily.service.DailyPostInteractionService;
import com.lifelink.notification.service.NotificationService;
import com.lifelink.relationship.service.RelationshipPermissionService;
import com.lifelink.timeline.service.RelationshipTimelineService;
import com.lifelink.user.entity.User;
import com.lifelink.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyPostInteractionServiceImpl implements DailyPostInteractionService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String DELETED_STATUS = "DELETED";

    private final DailyPostMapper dailyPostMapper;
    private final DailyPostLikeMapper dailyPostLikeMapper;
    private final DailyPostCommentMapper dailyPostCommentMapper;
    private final RelationshipPermissionService relationshipPermissionService;
    private final UserMapper userMapper;
    private final SpaceActivityService spaceActivityService;
    private final NotificationService notificationService;
    private final RelationshipTimelineService relationshipTimelineService;

    @Override
    @Transactional
    public DailyPostInteractionResponse likePost(Long postId, Long userId) {
        DailyPost post = requireActivePostAndMember(postId, userId);
        DailyPostLike existing = findLike(postId, userId);
        if (existing == null) {
            DailyPostLike like = new DailyPostLike();
            like.setDailyPostId(postId);
            like.setUserId(userId);
            like.setCreatedAt(LocalDateTime.now());
            dailyPostLikeMapper.insert(like);
            createNotificationSafely(
                    post.getUserId(),
                    userId,
                    "DAILY_POST_LIKED",
                    "Someone liked your daily post",
                    buildActorContent(userId, " liked your daily post"),
                    "DAILY_POST",
                    postId,
                    post.getRelationshipId(),
                    Map.of("postId", postId, "contentPreview", buildPreview(post.getContent()))
            );
        }
        return buildInteraction(postId, userId);
    }

    @Override
    @Transactional
    public DailyPostInteractionResponse unlikePost(Long postId, Long userId) {
        requireActivePostAndMember(postId, userId);
        DailyPostLike existing = findLike(postId, userId);
        if (existing != null) {
            dailyPostLikeMapper.deleteById(existing.getId());
        }
        return buildInteraction(postId, userId);
    }

    @Override
    @Transactional
    public DailyPostCommentResponse commentPost(Long postId, CommentDailyPostRequest request, Long userId) {
        DailyPost post = requireActivePostAndMember(postId, userId);
        DailyPostComment comment = new DailyPostComment();
        LocalDateTime now = LocalDateTime.now();
        comment.setDailyPostId(postId);
        comment.setUserId(userId);
        comment.setContent(request.getContent().trim());
        comment.setStatus(ACTIVE_STATUS);
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);
        dailyPostCommentMapper.insert(comment);
        createNotificationSafely(
                post.getUserId(),
                userId,
                "DAILY_POST_COMMENTED",
                "Someone commented on your daily post",
                buildActorContent(userId, " commented on your daily post"),
                "DAILY_POST_COMMENT",
                comment.getId(),
                post.getRelationshipId(),
                Map.of("postId", postId, "commentId", comment.getId(), "commentPreview", buildPreview(comment.getContent()))
        );
        createCommentActivitySafely(post, comment, userId);
        createImportantCommentTimelineSafely(post, comment, userId);
        return toCommentResponse(comment, userId);
    }

    @Override
    public List<DailyPostCommentResponse> listComments(Long postId, Integer page, Integer size, Long userId) {
        requireActivePostAndMember(postId, userId);
        long current = page == null || page < 1 ? 1L : page.longValue();
        long pageSize = size == null || size < 1 ? 20L : Math.min(size.longValue(), 100L);
        Page<DailyPostComment> result = dailyPostCommentMapper.selectPage(
                new Page<DailyPostComment>(current, pageSize),
                new LambdaQueryWrapper<DailyPostComment>()
                        .eq(DailyPostComment::getDailyPostId, postId)
                        .eq(DailyPostComment::getStatus, ACTIVE_STATUS)
                        .orderByAsc(DailyPostComment::getCreatedAt)
        );
        List<DailyPostCommentResponse> responses = new ArrayList<DailyPostCommentResponse>();
        for (DailyPostComment comment : result.getRecords()) {
            responses.add(toCommentResponse(comment, userId));
        }
        return responses;
    }

    @Override
    @Transactional
    public void deleteComment(Long postId, Long commentId, Long userId) {
        requireActivePostAndMember(postId, userId);
        DailyPostComment comment = dailyPostCommentMapper.selectById(commentId);
        if (comment == null || !postId.equals(comment.getDailyPostId()) || !ACTIVE_STATUS.equals(comment.getStatus())) {
            throw new BusinessException(404, "Comment not found");
        }
        if (!userId.equals(comment.getUserId())) {
            throw new BusinessException(403, "Only comment author can delete this comment");
        }
        comment.setStatus(DELETED_STATUS);
        comment.setUpdatedAt(LocalDateTime.now());
        dailyPostCommentMapper.updateById(comment);
    }

    @Override
    public DailyPostInteractionResponse getInteractionSummary(Long postId, Long userId) {
        requireActivePostAndMember(postId, userId);
        return buildInteraction(postId, userId);
    }

    private DailyPost requireActivePostAndMember(Long postId, Long userId) {
        DailyPost post = dailyPostMapper.selectById(postId);
        if (post == null || !ACTIVE_STATUS.equals(post.getStatus())) {
            throw new BusinessException(404, "Daily post not found");
        }
        relationshipPermissionService.requireActiveRelationshipMember(post.getRelationshipId(), userId);
        return post;
    }

    private DailyPostLike findLike(Long postId, Long userId) {
        return dailyPostLikeMapper.selectOne(new LambdaQueryWrapper<DailyPostLike>()
                .eq(DailyPostLike::getDailyPostId, postId)
                .eq(DailyPostLike::getUserId, userId)
                .last("LIMIT 1"));
    }

    private DailyPostInteractionResponse buildInteraction(Long postId, Long userId) {
        Long likeCount = dailyPostLikeMapper.selectCount(new LambdaQueryWrapper<DailyPostLike>()
                .eq(DailyPostLike::getDailyPostId, postId));
        Long commentCount = dailyPostCommentMapper.selectCount(new LambdaQueryWrapper<DailyPostComment>()
                .eq(DailyPostComment::getDailyPostId, postId)
                .eq(DailyPostComment::getStatus, ACTIVE_STATUS));
        return new DailyPostInteractionResponse(postId, likeCount, commentCount, findLike(postId, userId) != null);
    }

    private DailyPostCommentResponse toCommentResponse(DailyPostComment comment, Long currentUserId) {
        User user = userMapper.selectById(comment.getUserId());
        return new DailyPostCommentResponse(
                comment.getId(),
                comment.getDailyPostId(),
                comment.getUserId(),
                user == null ? null : user.getUsername(),
                user == null ? null : user.getAvatarUrl(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                currentUserId.equals(comment.getUserId())
        );
    }

    private void createCommentActivitySafely(DailyPost post, DailyPostComment comment, Long userId) {
        try {
            spaceActivityService.createActivity(
                    post.getRelationshipId(),
                    userId,
                    "DAILY_POST_COMMENTED",
                    "DAILY_POST_COMMENT",
                    comment.getId(),
                    "Commented on a daily post",
                    null,
                    Map.of("postId", post.getId(), "commentId", comment.getId(), "contentPreview", buildPreview(comment.getContent()))
            );
        } catch (Exception ex) {
            log.warn("Create daily comment activity failed", ex);
        }
    }

    private void createImportantCommentTimelineSafely(DailyPost post, DailyPostComment comment, Long userId) {
        try {
            Long commentCount = dailyPostCommentMapper.selectCount(new LambdaQueryWrapper<DailyPostComment>()
                    .eq(DailyPostComment::getDailyPostId, post.getId())
                    .eq(DailyPostComment::getStatus, ACTIVE_STATUS));
            if (commentCount == 5L) {
                relationshipTimelineService.createAutoEvent(
                        post.getRelationshipId(),
                        "IMPORTANT_COMMENT_INTERACTION",
                        "A meaningful interaction happened",
                        "This daily post received multiple comments",
                        userId,
                        "DAILY_POST",
                        post.getId(),
                        null,
                        null,
                        comment.getCreatedAt(),
                        "IMPORTANT",
                        Map.of("postId", post.getId(), "commentCount", commentCount, "contentPreview", buildPreview(post.getContent()))
                );
            }
        } catch (Exception ex) {
            log.warn("Create important comment timeline event failed", ex);
        }
    }

    private String buildPreview(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String trimmed = content.trim();
        return trimmed.length() > 30 ? trimmed.substring(0, 30) + "..." : trimmed;
    }

    private String buildActorContent(Long actorUserId, String suffix) {
        User actor = userMapper.selectById(actorUserId);
        return (actor == null ? "Someone" : actor.getUsername()) + suffix;
    }

    private void createNotificationSafely(Long receiverUserId, Long actorUserId, String notificationType, String title, String content, String relatedType, Long relatedId, Long relationshipId, Map<String, Object> metadata) {
        try {
            notificationService.createNotification(receiverUserId, actorUserId, notificationType, title, content, relatedType, relatedId, relationshipId, metadata);
        } catch (Exception ex) {
            log.warn("Create daily notification failed: {}", notificationType, ex);
        }
    }
}
