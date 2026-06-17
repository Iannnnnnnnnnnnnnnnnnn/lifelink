package com.lifelink.anniversary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifelink.activity.service.SpaceActivityService;
import com.lifelink.anniversary.dto.AnniversaryDetailResponse;
import com.lifelink.anniversary.dto.AnniversaryResponse;
import com.lifelink.anniversary.dto.CreateAnniversaryRequest;
import com.lifelink.anniversary.dto.UpdateAnniversaryRequest;
import com.lifelink.anniversary.entity.Anniversary;
import com.lifelink.anniversary.mapper.AnniversaryMapper;
import com.lifelink.anniversary.service.AnniversaryService;
import com.lifelink.common.BusinessException;
import com.lifelink.file.entity.FileResource;
import com.lifelink.file.mapper.FileResourceMapper;
import com.lifelink.file.service.FileUrlService;
import com.lifelink.notification.service.NotificationService;
import com.lifelink.relationship.entity.Relationship;
import com.lifelink.relationship.entity.RelationshipMember;
import com.lifelink.relationship.mapper.RelationshipMapper;
import com.lifelink.relationship.service.RelationshipPermissionService;
import com.lifelink.timeline.service.RelationshipTimelineService;
import com.lifelink.user.entity.User;
import com.lifelink.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnniversaryServiceImpl implements AnniversaryService {

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String DELETED_STATUS = "DELETED";
    private static final String REPEAT_NONE = "NONE";
    private static final String REPEAT_YEARLY = "YEARLY";
    private static final String DISPLAY_COUNTDOWN = "COUNTDOWN";
    private static final String DISPLAY_PASSED = "PASSED";
    private static final String DISPLAY_TODAY = "TODAY";

    private final AnniversaryMapper anniversaryMapper;
    private final RelationshipMapper relationshipMapper;
    private final RelationshipPermissionService relationshipPermissionService;
    private final FileResourceMapper fileResourceMapper;
    private final FileUrlService fileUrlService;
    private final SpaceActivityService spaceActivityService;
    private final NotificationService notificationService;
    private final UserMapper userMapper;
    private final RelationshipTimelineService relationshipTimelineService;

    @Override
    @Transactional
    public AnniversaryDetailResponse createAnniversary(CreateAnniversaryRequest request, Long userId) {
        Relationship relationship = requireActiveRelationship(request.getRelationshipId());
        requireMember(request.getRelationshipId(), userId);
        FileResource background = resolveBackground(request.getBackgroundFileId(), userId);
        LocalDateTime now = LocalDateTime.now();

        Anniversary anniversary = new Anniversary();
        anniversary.setRelationshipId(request.getRelationshipId());
        anniversary.setTitle(request.getTitle().trim());
        anniversary.setDescription(trimToNull(request.getDescription()));
        anniversary.setAnniversaryDate(request.getAnniversaryDate());
        anniversary.setRepeatType(StringUtils.hasText(request.getRepeatType()) ? request.getRepeatType() : REPEAT_NONE);
        anniversary.setBackgroundFileId(background == null ? null : background.getId());
        anniversary.setBackgroundUrl(background == null ? null : fileUrlService.buildPublicUrl(background));
        anniversary.setCreatedBy(userId);
        anniversary.setUpdatedBy(userId);
        anniversary.setStatus(ACTIVE_STATUS);
        anniversary.setCreatedAt(now);
        anniversary.setUpdatedAt(now);
        anniversaryMapper.insert(anniversary);
        createActivitySafely(
                anniversary.getRelationshipId(),
                userId,
                "ANNIVERSARY_CREATED",
                "ANNIVERSARY",
                anniversary.getId(),
                "Added anniversary: " + anniversary.getTitle(),
                null,
                Map.of("anniversaryTitle", anniversary.getTitle(), "anniversaryDate", anniversary.getAnniversaryDate().toString())
        );
        notifyRelationshipMembersSafely(
                anniversary.getRelationshipId(),
                userId,
                "ANNIVERSARY_CREATED",
                "New anniversary",
                "ANNIVERSARY",
                anniversary.getId(),
                Map.of("anniversaryTitle", anniversary.getTitle(), "anniversaryDate", anniversary.getAnniversaryDate().toString())
        );
        createTimelineEventSafely(
                anniversary.getRelationshipId(),
                "ANNIVERSARY_CREATED",
                "Added anniversary",
                "Added \"" + anniversary.getTitle() + "\"",
                userId,
                "ANNIVERSARY",
                anniversary.getId(),
                anniversary.getBackgroundFileId(),
                anniversary.getBackgroundUrl(),
                anniversary.getCreatedAt(),
                "IMPORTANT",
                Map.of("anniversaryTitle", anniversary.getTitle(), "anniversaryDate", anniversary.getAnniversaryDate().toString(), "repeatType", anniversary.getRepeatType())
        );

        return toDetail(anniversary, relationship, LocalDate.now());
    }

    @Override
    public List<AnniversaryResponse> listAnniversaries(Long relationshipId, String repeatType, String displayType, String keyword, Integer page, Integer size, Long userId) {
        List<Long> visibleRelationshipIds = resolveVisibleRelationshipIds(relationshipId, userId);
        if (visibleRelationshipIds.isEmpty()) {
            return new ArrayList<AnniversaryResponse>();
        }

        LambdaQueryWrapper<Anniversary> wrapper = new LambdaQueryWrapper<Anniversary>()
                .in(Anniversary::getRelationshipId, visibleRelationshipIds)
                .eq(Anniversary::getStatus, ACTIVE_STATUS);
        if (StringUtils.hasText(repeatType)) {
            wrapper.eq(Anniversary::getRepeatType, repeatType);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(item -> item.like(Anniversary::getTitle, keyword).or().like(Anniversary::getDescription, keyword));
        }
        wrapper.orderByDesc(Anniversary::getCreatedAt);

        LocalDate today = LocalDate.now();
        List<AnniversaryResponse> responses = new ArrayList<AnniversaryResponse>();
        for (Anniversary anniversary : anniversaryMapper.selectList(wrapper)) {
            AnniversaryResponse response = toResponse(anniversary, relationshipMapper.selectById(anniversary.getRelationshipId()), today);
            if (!StringUtils.hasText(displayType) || displayType.equals(response.getDisplayType())) {
                responses.add(response);
            }
        }
        responses.sort(displayOrder());
        int current = page == null || page < 1 ? 1 : page;
        int pageSize = size == null || size < 1 ? 20 : Math.min(size, 100);
        int fromIndex = Math.min((current - 1) * pageSize, responses.size());
        int toIndex = Math.min(fromIndex + pageSize, responses.size());
        return responses.subList(fromIndex, toIndex);
    }

    @Override
    public AnniversaryDetailResponse getAnniversaryDetail(Long id, Long userId) {
        Anniversary anniversary = requireActiveAnniversary(id);
        Relationship relationship = requireActiveRelationship(anniversary.getRelationshipId());
        requireMember(anniversary.getRelationshipId(), userId);
        return toDetail(anniversary, relationship, LocalDate.now());
    }

    @Override
    @Transactional
    public AnniversaryDetailResponse updateAnniversary(Long id, UpdateAnniversaryRequest request, Long userId) {
        Anniversary anniversary = requireActiveAnniversary(id);
        Relationship relationship = requireActiveRelationship(anniversary.getRelationshipId());
        requireMember(anniversary.getRelationshipId(), userId);
        FileResource background = resolveBackground(request.getBackgroundFileId(), userId);

        anniversary.setTitle(request.getTitle().trim());
        anniversary.setDescription(trimToNull(request.getDescription()));
        anniversary.setAnniversaryDate(request.getAnniversaryDate());
        anniversary.setRepeatType(StringUtils.hasText(request.getRepeatType()) ? request.getRepeatType() : REPEAT_NONE);
        anniversary.setBackgroundFileId(background == null ? null : background.getId());
        anniversary.setBackgroundUrl(background == null ? null : fileUrlService.buildPublicUrl(background));
        anniversary.setUpdatedBy(userId);
        anniversary.setUpdatedAt(LocalDateTime.now());
        anniversaryMapper.updateById(anniversary);
        return toDetail(anniversary, relationship, LocalDate.now());
    }

    @Override
    @Transactional
    public void deleteAnniversary(Long id, Long userId) {
        Anniversary anniversary = requireActiveAnniversary(id);
        requireActiveRelationship(anniversary.getRelationshipId());
        requireMember(anniversary.getRelationshipId(), userId);
        anniversary.setStatus(DELETED_STATUS);
        anniversary.setUpdatedBy(userId);
        anniversary.setUpdatedAt(LocalDateTime.now());
        anniversaryMapper.updateById(anniversary);
    }

    private Relationship requireActiveRelationship(Long relationshipId) {
        return relationshipPermissionService.requireActiveRelationship(relationshipId);
    }

    private void requireMember(Long relationshipId, Long userId) {
        relationshipPermissionService.requireActiveRelationshipMember(relationshipId, userId);
    }

    private Anniversary requireActiveAnniversary(Long id) {
        Anniversary anniversary = anniversaryMapper.selectById(id);
        if (anniversary == null || !ACTIVE_STATUS.equals(anniversary.getStatus())) {
            throw new BusinessException(404, "Anniversary not found");
        }
        return anniversary;
    }

    private FileResource resolveBackground(Long backgroundFileId, Long userId) {
        if (backgroundFileId == null) {
            return null;
        }
        FileResource resource = fileResourceMapper.selectById(backgroundFileId);
        if (resource == null || !userId.equals(resource.getUserId())) {
            throw new BusinessException(400, "Background image is invalid or not uploaded by current user");
        }
        String contentType = resource.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BusinessException(400, "Background file must be an image");
        }
        return resource;
    }

    private List<Long> resolveVisibleRelationshipIds(Long relationshipId, Long userId) {
        List<Long> relationshipIds = new ArrayList<Long>();
        if (relationshipId != null) {
            requireActiveRelationship(relationshipId);
            requireMember(relationshipId, userId);
            relationshipIds.add(relationshipId);
            return relationshipIds;
        }

        return relationshipPermissionService.listActiveRelationshipIds(userId);
    }

    private AnniversaryResponse toResponse(Anniversary anniversary, Relationship relationship, LocalDate today) {
        AnniversaryDisplay display = calculateDisplay(anniversary, today);
        return new AnniversaryResponse(
                anniversary.getId(),
                anniversary.getRelationshipId(),
                relationship == null ? null : relationship.getName(),
                anniversary.getTitle(),
                anniversary.getDescription(),
                anniversary.getAnniversaryDate(),
                anniversary.getRepeatType(),
                anniversary.getBackgroundFileId(),
                resolveBackgroundUrl(anniversary),
                display.dayCount(),
                display.displayType(),
                display.passedYears(),
                anniversary.getCreatedBy(),
                anniversary.getCreatedAt(),
                anniversary.getUpdatedAt()
        );
    }

    private String resolveBackgroundUrl(Anniversary anniversary) {
        if (anniversary.getBackgroundFileId() == null) {
            return anniversary.getBackgroundUrl();
        }
        FileResource resource = fileResourceMapper.selectById(anniversary.getBackgroundFileId());
        return resource == null ? anniversary.getBackgroundUrl() : fileUrlService.buildPublicUrl(resource);
    }

    private AnniversaryDetailResponse toDetail(Anniversary anniversary, Relationship relationship, LocalDate today) {
        AnniversaryResponse response = toResponse(anniversary, relationship, today);
        AnniversaryDetailResponse detail = new AnniversaryDetailResponse();
        detail.setId(response.getId());
        detail.setRelationshipId(response.getRelationshipId());
        detail.setRelationshipName(response.getRelationshipName());
        detail.setTitle(response.getTitle());
        detail.setDescription(response.getDescription());
        detail.setAnniversaryDate(response.getAnniversaryDate());
        detail.setRepeatType(response.getRepeatType());
        detail.setBackgroundFileId(response.getBackgroundFileId());
        detail.setBackgroundUrl(response.getBackgroundUrl());
        detail.setDayCount(response.getDayCount());
        detail.setDisplayType(response.getDisplayType());
        detail.setPassedYears(response.getPassedYears());
        detail.setCreatedBy(response.getCreatedBy());
        detail.setCreatedAt(response.getCreatedAt());
        detail.setUpdatedAt(response.getUpdatedAt());
        return detail;
    }

    private AnniversaryDisplay calculateDisplay(Anniversary anniversary, LocalDate today) {
        LocalDate date = anniversary.getAnniversaryDate();
        if (REPEAT_YEARLY.equals(anniversary.getRepeatType())) {
            int occurrenceYear = Math.max(today.getYear(), date.getYear());
            LocalDate occurrence = yearlyOccurrence(date, occurrenceYear);
            if (occurrence.isEqual(today)) {
                return new AnniversaryDisplay(DISPLAY_TODAY, 0L, Math.max(0, today.getYear() - date.getYear()));
            }
            if (occurrence.isBefore(today)) {
                occurrence = yearlyOccurrence(date, occurrenceYear + 1);
            }
            return new AnniversaryDisplay(DISPLAY_COUNTDOWN, ChronoUnit.DAYS.between(today, occurrence), Math.max(0, today.getYear() - date.getYear()));
        }

        if (date.isEqual(today)) {
            return new AnniversaryDisplay(DISPLAY_TODAY, 0L, null);
        }
        if (date.isAfter(today)) {
            return new AnniversaryDisplay(DISPLAY_COUNTDOWN, ChronoUnit.DAYS.between(today, date), null);
        }
        return new AnniversaryDisplay(DISPLAY_PASSED, ChronoUnit.DAYS.between(date, today), null);
    }

    private LocalDate yearlyOccurrence(LocalDate source, int year) {
        try {
            return source.withYear(year);
        } catch (DateTimeException ex) {
            // Feb 29 anniversaries are shown on Feb 28 during non-leap years.
            return LocalDate.of(year, 2, 28);
        }
    }

    private Comparator<AnniversaryResponse> displayOrder() {
        return Comparator
                .comparingInt((AnniversaryResponse item) -> {
                    if (DISPLAY_TODAY.equals(item.getDisplayType())) {
                        return 0;
                    }
                    if (DISPLAY_COUNTDOWN.equals(item.getDisplayType())) {
                        return 1;
                    }
                    return 2;
                })
                .thenComparing((AnniversaryResponse item) -> DISPLAY_PASSED.equals(item.getDisplayType()) ? -item.getDayCount() : item.getDayCount())
                .thenComparing(AnniversaryResponse::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private void createActivitySafely(Long relationshipId, Long actorUserId, String activityType, String targetType, Long targetId, String title, String content, Map<String, Object> metadata) {
        try {
            spaceActivityService.createActivity(relationshipId, actorUserId, activityType, targetType, targetId, title, content, metadata);
        } catch (Exception ex) {
            log.warn("Create anniversary activity failed: {}", activityType, ex);
        }
    }

    private void createTimelineEventSafely(Long relationshipId, String eventType, String title, String description, Long actorUserId,
                                           String targetType, Long targetId, Long coverFileId, String coverUrl, LocalDateTime eventDate,
                                           String importance, Map<String, Object> metadata) {
        try {
            relationshipTimelineService.createAutoEvent(relationshipId, eventType, title, description, actorUserId, targetType, targetId,
                    coverFileId, coverUrl, eventDate, importance, metadata);
        } catch (Exception ex) {
            log.warn("Create anniversary timeline event failed: {}", eventType, ex);
        }
    }

    private void notifyRelationshipMembersSafely(Long relationshipId, Long actorUserId, String notificationType, String title, String relatedType, Long relatedId, Map<String, Object> metadata) {
        try {
            User actor = userMapper.selectById(actorUserId);
            String actorName = actor == null ? "Someone" : actor.getUsername();
            List<RelationshipMember> members = relationshipPermissionService.listActiveMembers(relationshipId);
            for (RelationshipMember member : members) {
                notificationService.createNotification(
                        member.getUserId(),
                        actorUserId,
                        notificationType,
                        title,
                        actorName + " added an anniversary",
                        relatedType,
                        relatedId,
                        relationshipId,
                        metadata
                );
            }
        } catch (Exception ex) {
            log.warn("Create anniversary notification failed: {}", notificationType, ex);
        }
    }

    private record AnniversaryDisplay(String displayType, Long dayCount, Integer passedYears) {
    }
}
