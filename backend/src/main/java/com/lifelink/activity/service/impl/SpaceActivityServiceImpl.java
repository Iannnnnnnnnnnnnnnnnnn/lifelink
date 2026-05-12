package com.lifelink.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.activity.dto.SpaceActivityResponse;
import com.lifelink.activity.entity.SpaceActivity;
import com.lifelink.activity.mapper.SpaceActivityMapper;
import com.lifelink.activity.service.SpaceActivityService;
import com.lifelink.common.BusinessException;
import com.lifelink.relationship.entity.Relationship;
import com.lifelink.relationship.entity.RelationshipMember;
import com.lifelink.relationship.mapper.RelationshipMapper;
import com.lifelink.relationship.mapper.RelationshipMemberMapper;
import com.lifelink.user.entity.User;
import com.lifelink.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpaceActivityServiceImpl implements SpaceActivityService {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final SpaceActivityMapper spaceActivityMapper;
    private final RelationshipMapper relationshipMapper;
    private final RelationshipMemberMapper relationshipMemberMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void createActivity(Long relationshipId, Long actorUserId, String activityType, String targetType, Long targetId, String title, String content, Map<String, Object> metadata) {
        SpaceActivity activity = new SpaceActivity();
        activity.setRelationshipId(relationshipId);
        activity.setActorUserId(actorUserId);
        activity.setActivityType(activityType);
        activity.setTargetType(targetType);
        activity.setTargetId(targetId);
        activity.setTitle(title);
        activity.setContent(content);
        activity.setMetadata(writeMetadata(metadata));
        activity.setStatus(ACTIVE_STATUS);
        activity.setCreatedAt(LocalDateTime.now());
        spaceActivityMapper.insert(activity);
    }

    @Override
    public List<SpaceActivityResponse> listActivities(Long relationshipId, String activityType, Integer page, Integer size, Long userId) {
        requireMember(relationshipId, userId);
        requireActiveRelationship(relationshipId);
        LambdaQueryWrapper<SpaceActivity> wrapper = baseWrapper(activityType)
                .eq(SpaceActivity::getRelationshipId, relationshipId)
                .orderByDesc(SpaceActivity::getCreatedAt);
        return toResponses(selectPage(wrapper, page, size));
    }

    @Override
    public List<SpaceActivityResponse> listMyActivities(String activityType, Integer page, Integer size, Long userId) {
        List<Long> relationshipIds = listCurrentUserRelationshipIds(userId);
        if (relationshipIds.isEmpty()) {
            return new ArrayList<SpaceActivityResponse>();
        }
        LambdaQueryWrapper<SpaceActivity> wrapper = baseWrapper(activityType)
                .in(SpaceActivity::getRelationshipId, relationshipIds)
                .orderByDesc(SpaceActivity::getCreatedAt);
        return toResponses(selectPage(wrapper, page, size));
    }

    private LambdaQueryWrapper<SpaceActivity> baseWrapper(String activityType) {
        LambdaQueryWrapper<SpaceActivity> wrapper = new LambdaQueryWrapper<SpaceActivity>()
                .eq(SpaceActivity::getStatus, ACTIVE_STATUS);
        if (StringUtils.hasText(activityType)) {
            wrapper.eq(SpaceActivity::getActivityType, activityType);
        }
        return wrapper;
    }

    private List<SpaceActivity> selectPage(LambdaQueryWrapper<SpaceActivity> wrapper, Integer page, Integer size) {
        long current = page == null || page < 1 ? 1L : page.longValue();
        long pageSize = size == null || size < 1 ? 20L : Math.min(size.longValue(), 100L);
        return spaceActivityMapper.selectPage(new Page<SpaceActivity>(current, pageSize), wrapper).getRecords();
    }

    private void requireActiveRelationship(Long relationshipId) {
        Relationship relationship = relationshipMapper.selectById(relationshipId);
        if (relationship == null || !ACTIVE_STATUS.equals(relationship.getStatus())) {
            throw new BusinessException(404, "Relationship not found");
        }
    }

    private void requireMember(Long relationshipId, Long userId) {
        RelationshipMember member = relationshipMemberMapper.selectOne(new LambdaQueryWrapper<RelationshipMember>()
                .eq(RelationshipMember::getRelationshipId, relationshipId)
                .eq(RelationshipMember::getUserId, userId)
                .eq(RelationshipMember::getStatus, ACTIVE_STATUS)
                .last("LIMIT 1"));
        if (member == null) {
            throw new BusinessException(403, "You are not a member of this relationship");
        }
    }

    private List<Long> listCurrentUserRelationshipIds(Long userId) {
        List<RelationshipMember> members = relationshipMemberMapper.selectList(new LambdaQueryWrapper<RelationshipMember>()
                .eq(RelationshipMember::getUserId, userId)
                .eq(RelationshipMember::getStatus, ACTIVE_STATUS));
        List<Long> relationshipIds = new ArrayList<Long>();
        for (RelationshipMember member : members) {
            Relationship relationship = relationshipMapper.selectById(member.getRelationshipId());
            if (relationship != null && ACTIVE_STATUS.equals(relationship.getStatus())) {
                relationshipIds.add(member.getRelationshipId());
            }
        }
        return relationshipIds;
    }

    private List<SpaceActivityResponse> toResponses(List<SpaceActivity> activities) {
        List<SpaceActivityResponse> responses = new ArrayList<SpaceActivityResponse>();
        for (SpaceActivity activity : activities) {
            responses.add(toResponse(activity));
        }
        return responses;
    }

    private SpaceActivityResponse toResponse(SpaceActivity activity) {
        Relationship relationship = relationshipMapper.selectById(activity.getRelationshipId());
        User actor = userMapper.selectById(activity.getActorUserId());
        return new SpaceActivityResponse(
                activity.getId(),
                activity.getRelationshipId(),
                relationship == null ? null : relationship.getName(),
                activity.getActorUserId(),
                actor == null ? null : actor.getUsername(),
                actor == null ? null : actor.getAvatarUrl(),
                activity.getActivityType(),
                activity.getTargetType(),
                activity.getTargetId(),
                activity.getTitle(),
                activity.getContent(),
                readMetadata(activity.getMetadata()),
                activity.getCreatedAt()
        );
    }

    private String writeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception ex) {
            return null;
        }
    }

    private Map<String, Object> readMetadata(String metadata) {
        if (!StringUtils.hasText(metadata)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(metadata, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }
}
