package com.lifelink.daily.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifelink.activity.service.SpaceActivityService;
import com.lifelink.common.BusinessException;
import com.lifelink.daily.dto.CreateDailyPostRequest;
import com.lifelink.daily.dto.DailyPostDetailResponse;
import com.lifelink.daily.dto.DailyPostImageResponse;
import com.lifelink.daily.dto.DailyPostResponse;
import com.lifelink.daily.entity.DailyPostImage;
import com.lifelink.daily.entity.DailyPost;
import com.lifelink.daily.entity.DailyPostComment;
import com.lifelink.daily.entity.DailyPostLike;
import com.lifelink.daily.mapper.DailyPostCommentMapper;
import com.lifelink.daily.mapper.DailyPostImageMapper;
import com.lifelink.daily.mapper.DailyPostLikeMapper;
import com.lifelink.daily.mapper.DailyPostMapper;
import com.lifelink.daily.service.DailyPostService;
import com.lifelink.file.entity.FileResource;
import com.lifelink.file.mapper.FileResourceMapper;
import com.lifelink.relationship.entity.Relationship;
import com.lifelink.relationship.mapper.RelationshipMapper;
import com.lifelink.relationship.service.RelationshipPermissionService;
import com.lifelink.user.entity.User;
import com.lifelink.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyPostServiceImpl implements DailyPostService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String DELETED_STATUS = "DELETED";
    private static final String DEFAULT_VISIBILITY = "RELATIONSHIP";

    private final DailyPostMapper dailyPostMapper;
    private final RelationshipMapper relationshipMapper;
    private final RelationshipPermissionService relationshipPermissionService;
    private final UserMapper userMapper;
    private final FileResourceMapper fileResourceMapper;
    private final DailyPostImageMapper dailyPostImageMapper;
    private final SpaceActivityService spaceActivityService;
    private final DailyPostLikeMapper dailyPostLikeMapper;
    private final DailyPostCommentMapper dailyPostCommentMapper;

    @Override
    @Transactional
    public DailyPostDetailResponse createPost(CreateDailyPostRequest request, Long userId) {
        requireRelationshipMember(request.getRelationshipId(), userId);
        Relationship relationship = requireActiveRelationship(request.getRelationshipId());

        LocalDateTime now = LocalDateTime.now();
        DailyPost post = new DailyPost();
        post.setRelationshipId(request.getRelationshipId());
        post.setUserId(userId);
        post.setContent(request.getContent().trim());
        post.setMood(trimToNull(request.getMood()));
        post.setVisibility(StringUtils.hasText(request.getVisibility()) ? request.getVisibility().trim() : DEFAULT_VISIBILITY);
        post.setStatus(ACTIVE_STATUS);
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        dailyPostMapper.insert(post);
        bindImages(post.getId(), request.getImageIds(), userId, now);
        createActivitySafely(
                post.getRelationshipId(),
                userId,
                "DAILY_POST_CREATED",
                "DAILY_POST",
                post.getId(),
                "Posted a daily update",
                null,
                Map.of("contentPreview", buildPreview(post.getContent()), "mood", post.getMood() == null ? "" : post.getMood())
        );

        return toDetail(post, relationship, userId);
    }

    @Override
    public List<DailyPostResponse> listPosts(Long relationshipId, Integer page, Integer size, Long userId) {
        LambdaQueryWrapper<DailyPost> wrapper = new LambdaQueryWrapper<DailyPost>()
                .eq(DailyPost::getStatus, ACTIVE_STATUS)
                .orderByDesc(DailyPost::getCreatedAt);

        if (relationshipId != null) {
            requireRelationshipMember(relationshipId, userId);
            wrapper.eq(DailyPost::getRelationshipId, relationshipId);
        } else {
            List<Long> relationshipIds = listCurrentUserRelationshipIds(userId);
            if (relationshipIds.isEmpty()) {
                return new ArrayList<DailyPostResponse>();
            }
            wrapper.in(DailyPost::getRelationshipId, relationshipIds);
        }

        long current = page == null || page < 1 ? 1L : page.longValue();
        long pageSize = size == null || size < 1 ? 10L : Math.min(size.longValue(), 50L);
        Page<DailyPost> result = dailyPostMapper.selectPage(new Page<DailyPost>(current, pageSize), wrapper);

        List<DailyPostResponse> responses = new ArrayList<DailyPostResponse>();
        for (DailyPost post : result.getRecords()) {
            Relationship relationship = relationshipMapper.selectById(post.getRelationshipId());
            responses.add(toResponse(post, relationship, userId));
        }
        return responses;
    }

    @Override
    public DailyPostDetailResponse getPost(Long postId, Long userId) {
        DailyPost post = dailyPostMapper.selectById(postId);
        if (post == null || !ACTIVE_STATUS.equals(post.getStatus())) {
            throw new BusinessException(404, "Daily post not found");
        }
        Relationship relationship = requireActiveRelationship(post.getRelationshipId());
        requireRelationshipMember(post.getRelationshipId(), userId);
        return toDetail(post, relationship, userId);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        DailyPost post = dailyPostMapper.selectById(postId);
        if (post == null || !ACTIVE_STATUS.equals(post.getStatus())) {
            throw new BusinessException(404, "Daily post not found");
        }
        requireRelationshipMember(post.getRelationshipId(), userId);
        if (!userId.equals(post.getUserId())) {
            throw new BusinessException(403, "Only author can delete this daily post");
        }
        post.setStatus(DELETED_STATUS);
        post.setUpdatedAt(LocalDateTime.now());
        dailyPostMapper.updateById(post);
    }

    private Relationship requireActiveRelationship(Long relationshipId) {
        return relationshipPermissionService.requireActiveRelationship(relationshipId);
    }

    private void requireRelationshipMember(Long relationshipId, Long userId) {
        relationshipPermissionService.requireActiveRelationshipMember(relationshipId, userId);
    }

    private List<Long> listCurrentUserRelationshipIds(Long userId) {
        return relationshipPermissionService.listActiveRelationshipIds(userId);
    }

    private DailyPostResponse toResponse(DailyPost post, Relationship relationship, Long currentUserId) {
        User user = userMapper.selectById(post.getUserId());
        return new DailyPostResponse(
                post.getId(),
                post.getRelationshipId(),
                relationship == null ? null : relationship.getName(),
                post.getUserId(),
                user == null ? null : user.getUsername(),
                post.getContent(),
                post.getMood(),
                post.getVisibility(),
                post.getCreatedAt(),
                listPostImages(post.getId()),
                countLikes(post.getId()),
                countComments(post.getId()),
                isLikedByUser(post.getId(), currentUserId)
        );
    }

    private DailyPostDetailResponse toDetail(DailyPost post, Relationship relationship, Long currentUserId) {
        User user = userMapper.selectById(post.getUserId());
        return new DailyPostDetailResponse(
                post.getId(),
                post.getRelationshipId(),
                relationship == null ? null : relationship.getName(),
                post.getUserId(),
                user == null ? null : user.getUsername(),
                post.getContent(),
                post.getMood(),
                post.getVisibility(),
                post.getStatus(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                listPostImages(post.getId()),
                countLikes(post.getId()),
                countComments(post.getId()),
                isLikedByUser(post.getId(), currentUserId)
        );
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private void bindImages(Long postId, List<Long> imageIds, Long userId, LocalDateTime now) {
        if (imageIds == null || imageIds.isEmpty()) {
            return;
        }
        if (imageIds.size() > 9) {
            throw new BusinessException(400, "At most 9 images are allowed");
        }

        Set<Long> uniqueIds = new HashSet<Long>(imageIds);
        List<FileResource> resources = fileResourceMapper.selectList(new LambdaQueryWrapper<FileResource>()
                .in(FileResource::getId, uniqueIds)
                .eq(FileResource::getUserId, userId));
        if (resources.size() != uniqueIds.size()) {
            throw new BusinessException(400, "Some images are invalid or not uploaded by current user");
        }
        for (FileResource resource : resources) {
            if (!StringUtils.hasText(resource.getContentType()) || !resource.getContentType().toLowerCase().startsWith("image/")) {
                throw new BusinessException(400, "Daily post files must be images");
            }
        }

        int index = 0;
        for (Long imageId : imageIds) {
            DailyPostImage item = new DailyPostImage();
            item.setDailyPostId(postId);
            item.setFileId(imageId);
            item.setSortOrder(index++);
            item.setCreatedAt(now);
            dailyPostImageMapper.insert(item);
        }
    }

    private String buildPreview(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String trimmed = content.trim();
        return trimmed.length() > 30 ? trimmed.substring(0, 30) + "..." : trimmed;
    }

    private void createActivitySafely(Long relationshipId, Long actorUserId, String activityType, String targetType, Long targetId, String title, String content, Map<String, Object> metadata) {
        try {
            spaceActivityService.createActivity(relationshipId, actorUserId, activityType, targetType, targetId, title, content, metadata);
        } catch (Exception ex) {
            log.warn("Create daily activity failed: {}", activityType, ex);
        }
    }

    private List<DailyPostImageResponse> listPostImages(Long postId) {
        List<DailyPostImage> imageRefs = dailyPostImageMapper.selectList(new LambdaQueryWrapper<DailyPostImage>()
                .eq(DailyPostImage::getDailyPostId, postId)
                .orderByAsc(DailyPostImage::getSortOrder));
        if (imageRefs.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> fileIds = new ArrayList<Long>();
        for (DailyPostImage imageRef : imageRefs) {
            fileIds.add(imageRef.getFileId());
        }
        List<FileResource> resources = fileResourceMapper.selectList(new LambdaQueryWrapper<FileResource>()
                .in(FileResource::getId, fileIds));
        Map<Long, FileResource> fileMap = resources.stream().collect(Collectors.toMap(FileResource::getId, item -> item));

        List<DailyPostImageResponse> result = new ArrayList<DailyPostImageResponse>();
        for (DailyPostImage imageRef : imageRefs) {
            FileResource resource = fileMap.get(imageRef.getFileId());
            if (resource != null) {
                result.add(new DailyPostImageResponse(
                        resource.getId(),
                        resource.getFileUrl(),
                        resource.getOriginalName(),
                        imageRef.getSortOrder()
                ));
            }
        }
        return result;
    }

    private Long countLikes(Long postId) {
        return dailyPostLikeMapper.selectCount(new LambdaQueryWrapper<DailyPostLike>()
                .eq(DailyPostLike::getDailyPostId, postId));
    }

    private Long countComments(Long postId) {
        return dailyPostCommentMapper.selectCount(new LambdaQueryWrapper<DailyPostComment>()
                .eq(DailyPostComment::getDailyPostId, postId)
                .eq(DailyPostComment::getStatus, ACTIVE_STATUS));
    }

    private Boolean isLikedByUser(Long postId, Long userId) {
        return dailyPostLikeMapper.selectCount(new LambdaQueryWrapper<DailyPostLike>()
                .eq(DailyPostLike::getDailyPostId, postId)
                .eq(DailyPostLike::getUserId, userId)) > 0;
    }
}
