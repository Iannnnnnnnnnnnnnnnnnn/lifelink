package com.lifelink.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifelink.activity.service.SpaceActivityService;
import com.lifelink.common.BusinessException;
import com.lifelink.notification.service.NotificationService;
import com.lifelink.relationship.entity.RelationshipMember;
import com.lifelink.relationship.mapper.RelationshipMemberMapper;
import com.lifelink.relationship.service.RelationshipPermissionService;
import com.lifelink.todo.dto.CreateSpaceTodoRequest;
import com.lifelink.todo.dto.SpaceTodoResponse;
import com.lifelink.todo.dto.UpdateSpaceTodoRequest;
import com.lifelink.todo.entity.SpaceTodo;
import com.lifelink.todo.mapper.SpaceTodoMapper;
import com.lifelink.todo.service.SpaceTodoService;
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
public class SpaceTodoServiceImpl implements SpaceTodoService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String TODO_STATUS = "TODO";
    private static final String DONE_STATUS = "DONE";
    private static final String DELETED_STATUS = "DELETED";
    private static final String NORMAL_PRIORITY = "NORMAL";

    private final SpaceTodoMapper spaceTodoMapper;
    private final RelationshipMemberMapper relationshipMemberMapper;
    private final RelationshipPermissionService relationshipPermissionService;
    private final UserMapper userMapper;
    private final SpaceActivityService spaceActivityService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public SpaceTodoResponse createTodo(Long relationshipId, CreateSpaceTodoRequest request, Long userId) {
        requireMember(relationshipId, userId);
        LocalDateTime now = LocalDateTime.now();

        SpaceTodo todo = new SpaceTodo();
        todo.setRelationshipId(relationshipId);
        todo.setTitle(request.getTitle().trim());
        todo.setContent(request.getContent());
        todo.setPriority(StringUtils.hasText(request.getPriority()) ? request.getPriority() : NORMAL_PRIORITY);
        todo.setStatus(TODO_STATUS);
        todo.setDueTime(request.getDueTime());
        todo.setCreatedBy(userId);
        todo.setUpdatedBy(userId);
        todo.setCreatedAt(now);
        todo.setUpdatedAt(now);
        spaceTodoMapper.insert(todo);
        createActivitySafely(
                relationshipId,
                userId,
                "TODO_CREATED",
                "SPACE_TODO",
                todo.getId(),
                "Created todo: " + todo.getTitle(),
                null,
                Map.of("todoTitle", todo.getTitle(), "priority", todo.getPriority())
        );
        notifyRelationshipMembersSafely(relationshipId, userId, "TODO_CREATED", "New space todo", "SPACE_TODO", todo.getId(), Map.of("todoTitle", todo.getTitle()));
        return toResponse(todo);
    }

    @Override
    public List<SpaceTodoResponse> listTodos(Long relationshipId, String status, String keyword, Integer page, Integer size, Long userId) {
        requireMember(relationshipId, userId);

        LambdaQueryWrapper<SpaceTodo> wrapper = new LambdaQueryWrapper<SpaceTodo>()
                .eq(SpaceTodo::getRelationshipId, relationshipId)
                .ne(SpaceTodo::getStatus, DELETED_STATUS);
        if (StringUtils.hasText(status)) {
            wrapper.eq(SpaceTodo::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(item -> item.like(SpaceTodo::getTitle, keyword).or().like(SpaceTodo::getContent, keyword));
        }
        wrapper.orderByAsc(SpaceTodo::getStatus)
                .orderByAsc(SpaceTodo::getDueTime)
                .orderByDesc(SpaceTodo::getCreatedAt);

        long current = page == null || page < 1 ? 1L : page.longValue();
        long pageSize = size == null || size < 1 ? 20L : Math.min(size.longValue(), 100L);
        Page<SpaceTodo> result = spaceTodoMapper.selectPage(new Page<SpaceTodo>(current, pageSize), wrapper);
        List<SpaceTodoResponse> responses = new ArrayList<SpaceTodoResponse>();
        for (SpaceTodo todo : result.getRecords()) {
            responses.add(toResponse(todo));
        }
        return responses;
    }

    @Override
    public SpaceTodoResponse getTodoDetail(Long relationshipId, Long todoId, Long userId) {
        requireMember(relationshipId, userId);
        return toResponse(requireTodo(relationshipId, todoId));
    }

    @Override
    @Transactional
    public SpaceTodoResponse updateTodo(Long relationshipId, Long todoId, UpdateSpaceTodoRequest request, Long userId) {
        requireMember(relationshipId, userId);
        SpaceTodo todo = requireTodo(relationshipId, todoId);
        if (StringUtils.hasText(request.getTitle())) {
            todo.setTitle(request.getTitle().trim());
        }
        todo.setContent(request.getContent());
        if (StringUtils.hasText(request.getPriority())) {
            todo.setPriority(request.getPriority());
        }
        todo.setDueTime(request.getDueTime());
        todo.setUpdatedBy(userId);
        todo.setUpdatedAt(LocalDateTime.now());
        spaceTodoMapper.updateById(todo);
        return toResponse(todo);
    }

    @Override
    @Transactional
    public SpaceTodoResponse toggleTodoStatus(Long relationshipId, Long todoId, Long userId) {
        requireMember(relationshipId, userId);
        SpaceTodo todo = requireTodo(relationshipId, todoId);
        LocalDateTime now = LocalDateTime.now();
        if (TODO_STATUS.equals(todo.getStatus())) {
            todo.setStatus(DONE_STATUS);
            todo.setCompletedBy(userId);
            todo.setCompletedAt(now);
            createActivitySafely(
                    relationshipId,
                    userId,
                    "TODO_COMPLETED",
                    "SPACE_TODO",
                    todo.getId(),
                    "Completed todo: " + todo.getTitle(),
                    null,
                    Map.of("todoTitle", todo.getTitle())
            );
            notifyRelationshipMembersSafely(relationshipId, userId, "TODO_COMPLETED", "Todo completed", "SPACE_TODO", todo.getId(), Map.of("todoTitle", todo.getTitle()));
        } else if (DONE_STATUS.equals(todo.getStatus())) {
            todo.setStatus(TODO_STATUS);
            todo.setCompletedBy(null);
            todo.setCompletedAt(null);
            createActivitySafely(
                    relationshipId,
                    userId,
                    "TODO_REOPENED",
                    "SPACE_TODO",
                    todo.getId(),
                    "Reopened todo: " + todo.getTitle(),
                    null,
                    Map.of("todoTitle", todo.getTitle())
            );
            notifyRelationshipMembersSafely(relationshipId, userId, "TODO_REOPENED", "Todo reopened", "SPACE_TODO", todo.getId(), Map.of("todoTitle", todo.getTitle()));
        } else {
            throw new BusinessException(400, "Todo status cannot be toggled");
        }
        todo.setUpdatedBy(userId);
        todo.setUpdatedAt(now);
        spaceTodoMapper.updateById(todo);
        return toResponse(todo);
    }

    @Override
    @Transactional
    public void deleteTodo(Long relationshipId, Long todoId, Long userId) {
        requireMember(relationshipId, userId);
        SpaceTodo todo = requireTodo(relationshipId, todoId);
        todo.setStatus(DELETED_STATUS);
        todo.setUpdatedBy(userId);
        todo.setUpdatedAt(LocalDateTime.now());
        spaceTodoMapper.updateById(todo);
    }

    private void requireMember(Long relationshipId, Long userId) {
        relationshipPermissionService.requireActiveRelationshipMember(relationshipId, userId);
    }

    private SpaceTodo requireTodo(Long relationshipId, Long todoId) {
        SpaceTodo todo = spaceTodoMapper.selectById(todoId);
        if (todo == null || !relationshipId.equals(todo.getRelationshipId()) || DELETED_STATUS.equals(todo.getStatus())) {
            throw new BusinessException(404, "Todo not found");
        }
        return todo;
    }

    private SpaceTodoResponse toResponse(SpaceTodo todo) {
        User creator = userMapper.selectById(todo.getCreatedBy());
        return new SpaceTodoResponse(
                todo.getId(),
                todo.getRelationshipId(),
                todo.getTitle(),
                todo.getContent(),
                todo.getPriority(),
                todo.getStatus(),
                todo.getDueTime(),
                todo.getCreatedBy(),
                creator == null ? null : creator.getUsername(),
                todo.getUpdatedBy(),
                todo.getCompletedBy(),
                todo.getCompletedAt(),
                todo.getCreatedAt(),
                todo.getUpdatedAt()
        );
    }

    private void createActivitySafely(Long relationshipId, Long actorUserId, String activityType, String targetType, Long targetId, String title, String content, Map<String, Object> metadata) {
        try {
            spaceActivityService.createActivity(relationshipId, actorUserId, activityType, targetType, targetId, title, content, metadata);
        } catch (Exception ex) {
            log.warn("Create todo activity failed: {}", activityType, ex);
        }
    }

    private void notifyRelationshipMembersSafely(Long relationshipId, Long actorUserId, String notificationType, String title, String relatedType, Long relatedId, Map<String, Object> metadata) {
        try {
            User actor = userMapper.selectById(actorUserId);
            String actorName = actor == null ? "Someone" : actor.getUsername();
            List<RelationshipMember> members = relationshipMemberMapper.selectList(new LambdaQueryWrapper<RelationshipMember>()
                    .eq(RelationshipMember::getRelationshipId, relationshipId)
                    .eq(RelationshipMember::getStatus, ACTIVE_STATUS));
            for (RelationshipMember member : members) {
                notificationService.createNotification(
                        member.getUserId(),
                        actorUserId,
                        notificationType,
                        title,
                        actorName + " " + title.toLowerCase(),
                        relatedType,
                        relatedId,
                        relationshipId,
                        metadata
                );
            }
        } catch (Exception ex) {
            log.warn("Create todo notification failed: {}", notificationType, ex);
        }
    }
}
