package com.lifelink.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lifelink.activity.entity.SpaceActivity;
import com.lifelink.activity.mapper.SpaceActivityMapper;
import com.lifelink.anniversary.entity.Anniversary;
import com.lifelink.anniversary.mapper.AnniversaryMapper;
import com.lifelink.common.BusinessException;
import com.lifelink.daily.entity.DailyPost;
import com.lifelink.daily.mapper.DailyPostMapper;
import com.lifelink.relationship.entity.Relationship;
import com.lifelink.relationship.mapper.RelationshipMapper;
import com.lifelink.relationship.service.RelationshipPermissionService;
import com.lifelink.search.dto.SearchGroupResponse;
import com.lifelink.search.dto.SearchItemResponse;
import com.lifelink.search.dto.SearchResponse;
import com.lifelink.search.service.SearchService;
import com.lifelink.todo.entity.SpaceTodo;
import com.lifelink.todo.mapper.SpaceTodoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String DELETED_STATUS = "DELETED";
    private static final String RELATIONSHIP = "RELATIONSHIP";
    private static final String DAILY_POST = "DAILY_POST";
    private static final String TODO = "TODO";
    private static final String ANNIVERSARY = "ANNIVERSARY";
    private static final String ACTIVITY = "ACTIVITY";
    private static final List<String> SUPPORTED_TYPES = Arrays.asList(RELATIONSHIP, DAILY_POST, TODO, ANNIVERSARY, ACTIVITY);

    private final RelationshipMapper relationshipMapper;
    private final RelationshipPermissionService relationshipPermissionService;
    private final DailyPostMapper dailyPostMapper;
    private final SpaceTodoMapper spaceTodoMapper;
    private final AnniversaryMapper anniversaryMapper;
    private final SpaceActivityMapper spaceActivityMapper;

    @Override
    public SearchResponse search(String keyword, String types, Long userId, Integer page, Integer size) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (!StringUtils.hasText(normalizedKeyword)) {
            throw new BusinessException(400, "Keyword is required");
        }
        if (normalizedKeyword.length() > 100) {
            throw new BusinessException(400, "Keyword length must be at most 100");
        }

        int limit = normalizeSize(size);
        List<Long> relationshipIds = listVisibleRelationshipIds(userId);
        Map<Long, Relationship> relationshipMap = loadRelationshipMap(relationshipIds);
        Set<String> typeSet = resolveTypes(types);

        List<SearchGroupResponse> groups = new ArrayList<SearchGroupResponse>();
        if (typeSet.contains(RELATIONSHIP)) {
            groups.add(searchRelationships(normalizedKeyword, relationshipIds, relationshipMap, limit));
        }
        if (typeSet.contains(DAILY_POST)) {
            groups.add(searchDailyPosts(normalizedKeyword, relationshipIds, relationshipMap, limit));
        }
        if (typeSet.contains(TODO)) {
            groups.add(searchTodos(normalizedKeyword, relationshipIds, relationshipMap, limit));
        }
        if (typeSet.contains(ANNIVERSARY)) {
            groups.add(searchAnniversaries(normalizedKeyword, relationshipIds, relationshipMap, limit));
        }
        if (typeSet.contains(ACTIVITY)) {
            groups.add(searchActivities(normalizedKeyword, relationshipIds, relationshipMap, limit));
        }

        int totalCount = 0;
        for (SearchGroupResponse group : groups) {
            totalCount += group.getCount();
        }
        return new SearchResponse(normalizedKeyword, totalCount, groups);
    }

    private SearchGroupResponse searchRelationships(String keyword, List<Long> relationshipIds, Map<Long, Relationship> relationshipMap, int limit) {
        if (relationshipIds.isEmpty()) {
            return group(RELATIONSHIP, "Relationships", Collections.emptyList());
        }
        List<Relationship> records = relationshipMapper.selectList(new QueryWrapper<Relationship>()
                .in("id", relationshipIds)
                .eq("status", ACTIVE_STATUS)
                .and(wrapper -> wrapper
                        .apply("name ILIKE {0}", likePattern(keyword))
                        .or().apply("description ILIKE {0}", likePattern(keyword))
                        .or().apply("type ILIKE {0}", likePattern(keyword)))
                .orderByDesc("created_at")
                .last("LIMIT " + limit));
        List<SearchItemResponse> items = new ArrayList<SearchItemResponse>();
        for (Relationship relationship : records) {
            items.add(new SearchItemResponse(
                    relationship.getId(),
                    RELATIONSHIP,
                    relationship.getName(),
                    relationship.getDescription(),
                    null,
                    relationship.getId(),
                    relationship.getName(),
                    "/relationships/" + relationship.getId(),
                    relationship.getCreatedAt(),
                    Map.of("relationshipType", relationship.getType())
            ));
        }
        return group(RELATIONSHIP, "Relationships", items);
    }

    private SearchGroupResponse searchDailyPosts(String keyword, List<Long> relationshipIds, Map<Long, Relationship> relationshipMap, int limit) {
        if (relationshipIds.isEmpty()) {
            return group(DAILY_POST, "Daily Posts", Collections.emptyList());
        }
        List<DailyPost> records = dailyPostMapper.selectList(new QueryWrapper<DailyPost>()
                .in("relationship_id", relationshipIds)
                .eq("status", ACTIVE_STATUS)
                .and(wrapper -> wrapper
                        .apply("content ILIKE {0}", likePattern(keyword))
                        .or().apply("mood ILIKE {0}", likePattern(keyword)))
                .orderByDesc("created_at")
                .last("LIMIT " + limit));
        List<SearchItemResponse> items = new ArrayList<SearchItemResponse>();
        for (DailyPost post : records) {
            Relationship relationship = relationshipMap.get(post.getRelationshipId());
            items.add(new SearchItemResponse(
                    post.getId(),
                    DAILY_POST,
                    "Daily Post",
                    preview(post.getContent()),
                    null,
                    post.getRelationshipId(),
                    relationship == null ? null : relationship.getName(),
                    "/daily/" + post.getId(),
                    post.getCreatedAt(),
                    Map.of("mood", post.getMood() == null ? "" : post.getMood())
            ));
        }
        return group(DAILY_POST, "Daily Posts", items);
    }

    private SearchGroupResponse searchTodos(String keyword, List<Long> relationshipIds, Map<Long, Relationship> relationshipMap, int limit) {
        if (relationshipIds.isEmpty()) {
            return group(TODO, "Todos", Collections.emptyList());
        }
        List<SpaceTodo> records = spaceTodoMapper.selectList(new QueryWrapper<SpaceTodo>()
                .in("relationship_id", relationshipIds)
                .ne("status", DELETED_STATUS)
                .and(wrapper -> wrapper
                        .apply("title ILIKE {0}", likePattern(keyword))
                        .or().apply("content ILIKE {0}", likePattern(keyword))
                        .or().apply("priority ILIKE {0}", likePattern(keyword)))
                .orderByDesc("created_at")
                .last("LIMIT " + limit));
        List<SearchItemResponse> items = new ArrayList<SearchItemResponse>();
        for (SpaceTodo todo : records) {
            Relationship relationship = relationshipMap.get(todo.getRelationshipId());
            items.add(new SearchItemResponse(
                    todo.getId(),
                    TODO,
                    todo.getTitle(),
                    preview(todo.getContent()),
                    null,
                    todo.getRelationshipId(),
                    relationship == null ? null : relationship.getName(),
                    "/relationships/" + todo.getRelationshipId() + "/todos",
                    todo.getCreatedAt(),
                    Map.of("priority", todo.getPriority(), "status", todo.getStatus())
            ));
        }
        return group(TODO, "Todos", items);
    }

    private SearchGroupResponse searchAnniversaries(String keyword, List<Long> relationshipIds, Map<Long, Relationship> relationshipMap, int limit) {
        if (relationshipIds.isEmpty()) {
            return group(ANNIVERSARY, "Anniversaries", Collections.emptyList());
        }
        List<Anniversary> records = anniversaryMapper.selectList(new QueryWrapper<Anniversary>()
                .in("relationship_id", relationshipIds)
                .eq("status", ACTIVE_STATUS)
                .and(wrapper -> wrapper
                        .apply("title ILIKE {0}", likePattern(keyword))
                        .or().apply("description ILIKE {0}", likePattern(keyword)))
                .orderByDesc("created_at")
                .last("LIMIT " + limit));
        List<SearchItemResponse> items = new ArrayList<SearchItemResponse>();
        for (Anniversary anniversary : records) {
            Relationship relationship = relationshipMap.get(anniversary.getRelationshipId());
            items.add(new SearchItemResponse(
                    anniversary.getId(),
                    ANNIVERSARY,
                    anniversary.getTitle(),
                    preview(anniversary.getDescription()),
                    null,
                    anniversary.getRelationshipId(),
                    relationship == null ? null : relationship.getName(),
                    "/anniversaries/" + anniversary.getId(),
                    anniversary.getCreatedAt(),
                    Map.of("repeatType", anniversary.getRepeatType(), "anniversaryDate", anniversary.getAnniversaryDate().toString())
            ));
        }
        return group(ANNIVERSARY, "Anniversaries", items);
    }

    private SearchGroupResponse searchActivities(String keyword, List<Long> relationshipIds, Map<Long, Relationship> relationshipMap, int limit) {
        if (relationshipIds.isEmpty()) {
            return group(ACTIVITY, "Activities", Collections.emptyList());
        }
        List<SpaceActivity> records = spaceActivityMapper.selectList(new QueryWrapper<SpaceActivity>()
                .in("relationship_id", relationshipIds)
                .eq("status", ACTIVE_STATUS)
                .and(wrapper -> wrapper
                        .apply("title ILIKE {0}", likePattern(keyword))
                        .or().apply("content ILIKE {0}", likePattern(keyword))
                        .or().apply("activity_type ILIKE {0}", likePattern(keyword)))
                .orderByDesc("created_at")
                .last("LIMIT " + limit));
        List<SearchItemResponse> items = new ArrayList<SearchItemResponse>();
        for (SpaceActivity activity : records) {
            Relationship relationship = relationshipMap.get(activity.getRelationshipId());
            items.add(new SearchItemResponse(
                    activity.getId(),
                    ACTIVITY,
                    activity.getTitle(),
                    preview(activity.getContent()),
                    null,
                    activity.getRelationshipId(),
                    relationship == null ? null : relationship.getName(),
                    "/relationships/" + activity.getRelationshipId() + "/activities",
                    activity.getCreatedAt(),
                    Map.of("activityType", activity.getActivityType(), "targetType", activity.getTargetType() == null ? "" : activity.getTargetType())
            ));
        }
        return group(ACTIVITY, "Activities", items);
    }

    private List<Long> listVisibleRelationshipIds(Long userId) {
        return relationshipPermissionService.listActiveRelationshipIds(userId);
    }

    private Map<Long, Relationship> loadRelationshipMap(List<Long> relationshipIds) {
        if (relationshipIds.isEmpty()) {
            return new HashMap<Long, Relationship>();
        }
        List<Relationship> relationships = relationshipMapper.selectList(new LambdaQueryWrapper<Relationship>()
                .in(Relationship::getId, relationshipIds)
                .eq(Relationship::getStatus, ACTIVE_STATUS));
        return relationships.stream().collect(Collectors.toMap(Relationship::getId, item -> item));
    }

    private Set<String> resolveTypes(String types) {
        if (!StringUtils.hasText(types)) {
            return new LinkedHashSet<String>(SUPPORTED_TYPES);
        }
        Set<String> result = new LinkedHashSet<String>();
        Set<String> supported = new HashSet<String>(SUPPORTED_TYPES);
        for (String type : types.split(",")) {
            String normalized = type.trim().toUpperCase();
            if (supported.contains(normalized)) {
                result.add(normalized);
            }
        }
        return result.isEmpty() ? new LinkedHashSet<String>(SUPPORTED_TYPES) : result;
    }

    private SearchGroupResponse group(String type, String title, List<SearchItemResponse> items) {
        return new SearchGroupResponse(type, title, items.size(), items);
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return 8;
        }
        return Math.min(size, 20);
    }

    private String likePattern(String keyword) {
        return "%" + keyword + "%";
    }

    private String preview(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() > 120 ? trimmed.substring(0, 120) + "..." : trimmed;
    }
}
