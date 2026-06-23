package com.lifelink.focus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.coin.service.FocusCoinService;
import com.lifelink.common.BusinessException;
import com.lifelink.focus.dto.CreateFocusRoomRequest;
import com.lifelink.focus.dto.FocusCalendarEventResponse;
import com.lifelink.focus.dto.FocusRoomResponse;
import com.lifelink.focus.dto.FocusSessionResponse;
import com.lifelink.focus.dto.FocusSettingsResponse;
import com.lifelink.focus.dto.FocusStatsResponse;
import com.lifelink.focus.dto.StartFocusSessionRequest;
import com.lifelink.focus.dto.UpdateFocusSettingsRequest;
import com.lifelink.focus.entity.FocusRoom;
import com.lifelink.focus.entity.FocusRoomMember;
import com.lifelink.focus.entity.FocusSession;
import com.lifelink.focus.entity.FocusSessionEvent;
import com.lifelink.focus.entity.FocusSettings;
import com.lifelink.focus.mapper.FocusRoomMapper;
import com.lifelink.focus.mapper.FocusRoomMemberMapper;
import com.lifelink.focus.mapper.FocusSessionEventMapper;
import com.lifelink.focus.mapper.FocusSessionMapper;
import com.lifelink.focus.mapper.FocusSettingsMapper;
import com.lifelink.focus.service.FocusService;
import com.lifelink.notification.service.NotificationService;
import com.lifelink.relationship.entity.Relationship;
import com.lifelink.relationship.entity.RelationshipMember;
import com.lifelink.relationship.mapper.RelationshipMapper;
import com.lifelink.relationship.service.RelationshipPermissionService;
import com.lifelink.todo.entity.SpaceTodo;
import com.lifelink.todo.mapper.SpaceTodoMapper;
import com.lifelink.user.entity.User;
import com.lifelink.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FocusServiceImpl implements FocusService {

    private static final String RUNNING = "RUNNING";
    private static final String PAUSED = "PAUSED";
    private static final String COMPLETED = "COMPLETED";
    private static final String ABANDONED = "ABANDONED";
    private static final String EXPIRED = "EXPIRED";
    private static final String FOCUS = "FOCUS";
    private static final String PERSONAL = "PERSONAL";
    private static final String SPACE = "SPACE";
    private static final String COUPLE = "COUPLE";
    private static final String MANUAL = "MANUAL";
    private static final String WAITING = "WAITING";
    private static final String ROOM_RUNNING = "RUNNING";
    private static final String ROOM_COMPLETED = "COMPLETED";
    private static final String INVITED = "INVITED";
    private static final String JOINED = "JOINED";
    private static final String FOCUSING = "FOCUSING";
    private static final String MEMBER_PAUSED = "PAUSED";
    private static final String DECLINED = "DECLINED";
    private static final String MEMBER_COMPLETED = "COMPLETED";
    private static final String MEMBER_ABANDONED = "ABANDONED";
    private static final int DEFAULT_FOCUS_MINUTES = 25;
    private static final int DEFAULT_SHORT_BREAK_MINUTES = 5;
    private static final int DEFAULT_LONG_BREAK_MINUTES = 15;
    private static final int DEFAULT_LONG_BREAK_INTERVAL = 4;

    private final FocusSettingsMapper focusSettingsMapper;
    private final FocusSessionMapper focusSessionMapper;
    private final FocusSessionEventMapper focusSessionEventMapper;
    private final FocusRoomMapper focusRoomMapper;
    private final FocusRoomMemberMapper focusRoomMemberMapper;
    private final SpaceTodoMapper spaceTodoMapper;
    private final RelationshipMapper relationshipMapper;
    private final UserMapper userMapper;
    private final RelationshipPermissionService relationshipPermissionService;
    private final NotificationService notificationService;
    private final FocusCoinService focusCoinService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public FocusSettingsResponse getSettings(Long userId) {
        return toSettingsResponse(getOrCreateSettings(userId));
    }

    @Override
    @Transactional
    public FocusSettingsResponse updateSettings(UpdateFocusSettingsRequest request, Long userId) {
        FocusSettings settings = getOrCreateSettings(userId);
        if (request.getFocusMinutes() != null) settings.setFocusMinutes(request.getFocusMinutes());
        if (request.getShortBreakMinutes() != null) settings.setShortBreakMinutes(request.getShortBreakMinutes());
        if (request.getLongBreakMinutes() != null) settings.setLongBreakMinutes(request.getLongBreakMinutes());
        if (request.getLongBreakInterval() != null) settings.setLongBreakInterval(request.getLongBreakInterval());
        if (request.getAutoStartBreak() != null) settings.setAutoStartBreak(request.getAutoStartBreak());
        if (request.getAutoStartNextFocus() != null) settings.setAutoStartNextFocus(request.getAutoStartNextFocus());
        if (request.getSoundEnabled() != null) settings.setSoundEnabled(request.getSoundEnabled());
        if (request.getNotificationEnabled() != null) settings.setNotificationEnabled(request.getNotificationEnabled());
        if (request.getStrictModeEnabled() != null) settings.setStrictModeEnabled(request.getStrictModeEnabled());
        settings.setUpdatedAt(LocalDateTime.now());
        focusSettingsMapper.updateById(settings);
        return toSettingsResponse(settings);
    }

    @Override
    @Transactional
    public FocusSessionResponse startSession(StartFocusSessionRequest request, Long userId) {
        expireStaleCurrent(userId);
        FocusSession current = findCurrentSession(userId);
        if (current != null) {
            throw new BusinessException(400, "A focus session is already running");
        }

        SpaceTodo todo = null;
        Long spaceId = request.getSpaceId();
        if (request.getTodoId() != null) {
            todo = requireVisibleTodo(request.getTodoId(), userId);
            if (spaceId != null && !spaceId.equals(todo.getRelationshipId())) {
                throw new BusinessException(400, "Todo does not belong to this space");
            }
            spaceId = todo.getRelationshipId();
        }
        if (spaceId != null) {
            relationshipPermissionService.requireActiveRelationshipMember(spaceId, userId);
        }

        LocalDateTime now = LocalDateTime.now();
        FocusSession session = new FocusSession();
        session.setUserId(userId);
        session.setSpaceId(spaceId);
        session.setTodoId(request.getTodoId());
        session.setRoomId(request.getRoomId());
        session.setSessionType(request.getRoomId() != null ? COUPLE : (spaceId != null ? SPACE : PERSONAL));
        session.setPhase(normalizePhase(request.getPhase()));
        session.setPlannedMinutes(resolvePlannedMinutes(request.getPlannedMinutes(), userId));
        session.setActualMinutes(0);
        session.setStartedAt(now);
        session.setPausedSeconds(0);
        session.setStatus(RUNNING);
        session.setSource(StringUtils.hasText(request.getSource()) ? request.getSource().trim().toUpperCase() : MANUAL);
        session.setNote(trimToNull(request.getNote()));
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        focusSessionMapper.insert(session);
        recordEvent(session, "START", Map.of("phase", session.getPhase(), "source", session.getSource()));
        return toSessionResponse(session);
    }

    @Override
    @Transactional
    public FocusSessionResponse pauseSession(Long sessionId, Long userId) {
        FocusSession session = requireOwnSession(sessionId, userId);
        if (!RUNNING.equals(session.getStatus())) {
            throw new BusinessException(400, "Only running sessions can be paused");
        }
        session.setStatus(PAUSED);
        session.setUpdatedAt(LocalDateTime.now());
        focusSessionMapper.updateById(session);
        recordEvent(session, "PAUSE", null);
        syncRoomMemberStatus(session.getRoomId(), userId, MEMBER_PAUSED);
        return toSessionResponse(session);
    }

    @Override
    @Transactional
    public FocusSessionResponse resumeSession(Long sessionId, Long userId) {
        FocusSession session = requireOwnSession(sessionId, userId);
        if (!PAUSED.equals(session.getStatus())) {
            throw new BusinessException(400, "Only paused sessions can be resumed");
        }
        session.setPausedSeconds(currentPausedSeconds(session));
        session.setStatus(RUNNING);
        session.setUpdatedAt(LocalDateTime.now());
        focusSessionMapper.updateById(session);
        recordEvent(session, "RESUME", Map.of("pausedSeconds", session.getPausedSeconds()));
        syncRoomMemberStatus(session.getRoomId(), userId, FOCUSING);
        return toSessionResponse(session);
    }

    @Override
    @Transactional
    public FocusSessionResponse completeSession(Long sessionId, Long userId) {
        FocusSession session = requireOwnSession(sessionId, userId);
        if (!RUNNING.equals(session.getStatus()) && !PAUSED.equals(session.getStatus())) {
            throw new BusinessException(400, "Only active sessions can be completed");
        }
        LocalDateTime now = LocalDateTime.now();
        session.setPausedSeconds(currentPausedSeconds(session));
        session.setStatus(COMPLETED);
        session.setEndedAt(now);
        session.setActualMinutes(calculateActualMinutes(session, now));
        session.setUpdatedAt(now);
        focusSessionMapper.updateById(session);
        recordEvent(session, "COMPLETE", Map.of("actualMinutes", session.getActualMinutes()));
        focusCoinService.awardForFocusSession(session);
        notifySessionCompleted(session);
        syncRoomMemberStatus(session.getRoomId(), userId, MEMBER_COMPLETED);
        completeRoomIfDone(session.getRoomId());
        return toSessionResponse(session);
    }

    @Override
    @Transactional
    public FocusSessionResponse abandonSession(Long sessionId, Long userId) {
        FocusSession session = requireOwnSession(sessionId, userId);
        if (!RUNNING.equals(session.getStatus()) && !PAUSED.equals(session.getStatus())) {
            throw new BusinessException(400, "Only active sessions can be abandoned");
        }
        LocalDateTime now = LocalDateTime.now();
        session.setPausedSeconds(currentPausedSeconds(session));
        session.setStatus(ABANDONED);
        session.setEndedAt(now);
        session.setActualMinutes(calculateActualMinutes(session, now));
        session.setUpdatedAt(now);
        focusSessionMapper.updateById(session);
        recordEvent(session, "ABANDON", Map.of("actualMinutes", session.getActualMinutes()));
        syncRoomMemberStatus(session.getRoomId(), userId, MEMBER_ABANDONED);
        completeRoomIfDone(session.getRoomId());
        return toSessionResponse(session);
    }

    @Override
    @Transactional
    public FocusSessionResponse getCurrentSession(Long userId) {
        expireStaleCurrent(userId);
        FocusSession session = findCurrentSession(userId);
        return session == null ? null : toSessionResponse(session);
    }

    @Override
    public List<FocusSessionResponse> listSessions(String startDate, String endDate, Long userId) {
        LocalDateTime start = parseStart(startDate, LocalDate.now().minusDays(14));
        LocalDateTime end = parseEnd(endDate, LocalDate.now().plusDays(1));
        List<FocusSession> sessions = focusSessionMapper.selectList(new LambdaQueryWrapper<FocusSession>()
                .eq(FocusSession::getUserId, userId)
                .ge(FocusSession::getStartedAt, start)
                .lt(FocusSession::getStartedAt, end)
                .orderByDesc(FocusSession::getStartedAt));
        return sessions.stream().map(this::toSessionResponse).toList();
    }

    @Override
    public FocusStatsResponse getStats(String range, Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = switch (range) {
            case "month" -> today.withDayOfMonth(1);
            case "week" -> today.minusDays(6);
            default -> today;
        };
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        List<FocusSession> sessions = focusSessionMapper.selectList(new LambdaQueryWrapper<FocusSession>()
                .eq(FocusSession::getUserId, userId)
                .ge(FocusSession::getStartedAt, start)
                .lt(FocusSession::getStartedAt, end));
        Integer completed = countStatus(sessions, COMPLETED);
        Integer abandoned = countStatus(sessions, ABANDONED);
        Integer minutes = sessions.stream()
                .filter(item -> COMPLETED.equals(item.getStatus()))
                .mapToInt(item -> safeInt(item.getActualMinutes()))
                .sum();
        Integer weekMinutes = getWeekMinutes(userId, today);
        double completionRate = completed + abandoned == 0 ? 0 : completed * 1.0 / (completed + abandoned);
        return new FocusStatsResponse(
                minutes,
                completed,
                abandoned,
                completionRate,
                getCurrentStreak(userId),
                weekMinutes,
                buildTopTodos(sessions),
                buildDailyTrend(userId, startDate, today)
        );
    }

    @Override
    @Transactional
    public FocusRoomResponse createRoom(CreateFocusRoomRequest request, Long userId) {
        relationshipPermissionService.requireActiveRelationshipMember(request.getSpaceId(), userId);
        Set<Long> inviteUserIds = new LinkedHashSet<Long>();
        if (request.getInviteUserIds() != null) {
            inviteUserIds.addAll(request.getInviteUserIds());
        }
        inviteUserIds.remove(userId);
        for (Long inviteUserId : inviteUserIds) {
            relationshipPermissionService.requireActiveRelationshipMember(request.getSpaceId(), inviteUserId);
        }

        LocalDateTime now = LocalDateTime.now();
        FocusRoom room = new FocusRoom();
        room.setCreatorUserId(userId);
        room.setSpaceId(request.getSpaceId());
        room.setTitle(StringUtils.hasText(request.getTitle()) ? request.getTitle().trim() : "Focus together");
        room.setPlannedMinutes(request.getPlannedMinutes() == null ? DEFAULT_FOCUS_MINUTES : request.getPlannedMinutes());
        room.setStatus(WAITING);
        room.setCreatedAt(now);
        room.setUpdatedAt(now);
        focusRoomMapper.insert(room);

        createRoomMember(room.getId(), userId, JOINED, now);
        for (Long inviteUserId : inviteUserIds) {
            createRoomMember(room.getId(), inviteUserId, INVITED, now);
            createNotificationSafely(inviteUserId, userId, "FOCUS_ROOM_INVITE", "一起专注邀请",
                    "对方邀请你一起专注 " + room.getPlannedMinutes() + " 分钟。", "FOCUS_ROOM", room.getId(), room.getSpaceId(),
                    Map.of("plannedMinutes", room.getPlannedMinutes()));
        }
        return toRoomResponse(room);
    }

    @Override
    public FocusRoomResponse getRoom(Long roomId, Long userId) {
        FocusRoom room = requireRoom(roomId);
        requireRoomMember(roomId, userId);
        relationshipPermissionService.requireActiveRelationshipMember(room.getSpaceId(), userId);
        return toRoomResponse(room);
    }

    @Override
    @Transactional
    public FocusRoomResponse joinRoom(Long roomId, Long userId) {
        FocusRoom room = requireRoom(roomId);
        relationshipPermissionService.requireActiveRelationshipMember(room.getSpaceId(), userId);
        FocusRoomMember member = requireRoomMember(roomId, userId);
        if (!INVITED.equals(member.getMemberStatus()) && !JOINED.equals(member.getMemberStatus())) {
            throw new BusinessException(400, "Cannot join this focus room");
        }
        member.setMemberStatus(JOINED);
        member.setJoinedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        focusRoomMemberMapper.updateById(member);
        return toRoomResponse(room);
    }

    @Override
    @Transactional
    public FocusRoomResponse declineRoom(Long roomId, Long userId) {
        FocusRoom room = requireRoom(roomId);
        relationshipPermissionService.requireActiveRelationshipMember(room.getSpaceId(), userId);
        FocusRoomMember member = requireRoomMember(roomId, userId);
        member.setMemberStatus(DECLINED);
        member.setUpdatedAt(LocalDateTime.now());
        focusRoomMemberMapper.updateById(member);
        completeRoomIfDone(roomId);
        return toRoomResponse(focusRoomMapper.selectById(room.getId()));
    }

    @Override
    @Transactional
    public FocusRoomResponse startRoom(Long roomId, Long userId) {
        FocusRoom room = requireRoom(roomId);
        relationshipPermissionService.requireActiveRelationshipMember(room.getSpaceId(), userId);
        if (!userId.equals(room.getCreatorUserId())) {
            throw new BusinessException(403, "Only creator can start this focus room");
        }
        if (!WAITING.equals(room.getStatus())) {
            throw new BusinessException(400, "Focus room cannot be started");
        }
        LocalDateTime now = LocalDateTime.now();
        room.setStatus(ROOM_RUNNING);
        room.setStartedAt(now);
        room.setUpdatedAt(now);
        focusRoomMapper.updateById(room);

        List<FocusRoomMember> members = listRoomMembers(roomId);
        for (FocusRoomMember member : members) {
            if (JOINED.equals(member.getMemberStatus())) {
                member.setMemberStatus(FOCUSING);
                member.setUpdatedAt(now);
                focusRoomMemberMapper.updateById(member);
                startRoomSession(room, member.getUserId(), now);
                createNotificationSafely(member.getUserId(), userId, "FOCUS_ROOM_STARTED", "一起专注开始",
                        "一起专注已经开始。", "FOCUS_ROOM", room.getId(), room.getSpaceId(), Map.of("plannedMinutes", room.getPlannedMinutes()));
            }
        }
        return toRoomResponse(room);
    }

    @Override
    @Transactional
    public FocusRoomResponse completeRoom(Long roomId, Long userId) {
        FocusRoom room = requireRoom(roomId);
        relationshipPermissionService.requireActiveRelationshipMember(room.getSpaceId(), userId);
        FocusSession session = findRoomActiveSession(roomId, userId);
        if (session != null) {
            completeSession(session.getId(), userId);
        } else {
            syncRoomMemberStatus(roomId, userId, MEMBER_COMPLETED);
            completeRoomIfDone(roomId);
        }
        return toRoomResponse(focusRoomMapper.selectById(room.getId()));
    }

    @Override
    @Transactional
    public FocusRoomResponse abandonRoom(Long roomId, Long userId) {
        FocusRoom room = requireRoom(roomId);
        relationshipPermissionService.requireActiveRelationshipMember(room.getSpaceId(), userId);
        FocusSession session = findRoomActiveSession(roomId, userId);
        if (session != null) {
            abandonSession(session.getId(), userId);
        } else {
            syncRoomMemberStatus(roomId, userId, MEMBER_ABANDONED);
            completeRoomIfDone(roomId);
        }
        return toRoomResponse(focusRoomMapper.selectById(room.getId()));
    }

    @Override
    public FocusRoomResponse getCurrentRoom(Long userId) {
        List<FocusRoomMember> members = focusRoomMemberMapper.selectList(new LambdaQueryWrapper<FocusRoomMember>()
                .eq(FocusRoomMember::getUserId, userId)
                .in(FocusRoomMember::getMemberStatus, List.of(INVITED, JOINED, FOCUSING, MEMBER_PAUSED))
                .orderByDesc(FocusRoomMember::getUpdatedAt));
        for (FocusRoomMember member : members) {
            FocusRoom room = focusRoomMapper.selectById(member.getRoomId());
            if (room != null && (WAITING.equals(room.getStatus()) || ROOM_RUNNING.equals(room.getStatus()))) {
                return toRoomResponse(room);
            }
        }
        return null;
    }

    @Override
    public List<FocusRoomResponse> listRooms(Long spaceId, Long userId) {
        List<FocusRoomMember> memberships = focusRoomMemberMapper.selectList(new LambdaQueryWrapper<FocusRoomMember>()
                .eq(FocusRoomMember::getUserId, userId)
                .orderByDesc(FocusRoomMember::getUpdatedAt));
        List<FocusRoomResponse> responses = new ArrayList<FocusRoomResponse>();
        for (FocusRoomMember membership : memberships) {
            FocusRoom room = focusRoomMapper.selectById(membership.getRoomId());
            if (room == null) {
                continue;
            }
            if (spaceId != null && !spaceId.equals(room.getSpaceId())) {
                continue;
            }
            if (relationshipPermissionService.isActiveRelationshipMember(room.getSpaceId(), userId)) {
                responses.add(toRoomResponse(room));
            }
        }
        return responses;
    }

    @Override
    public List<FocusCalendarEventResponse> getCalendarEvents(Integer year, Integer month, Long userId) {
        YearMonth yearMonth = YearMonth.of(year == null ? YearMonth.now().getYear() : year, month == null ? YearMonth.now().getMonthValue() : month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();
        List<FocusSession> sessions = focusSessionMapper.selectList(new LambdaQueryWrapper<FocusSession>()
                .eq(FocusSession::getUserId, userId)
                .eq(FocusSession::getStatus, COMPLETED)
                .ge(FocusSession::getStartedAt, start)
                .lt(FocusSession::getStartedAt, end)
                .orderByAsc(FocusSession::getStartedAt));
        return sessions.stream()
                .map(session -> new FocusCalendarEventResponse(session.getId(), session.getSpaceId(), session.getTodoId(), getTodoTitle(session.getTodoId()), session.getRoomId(), session.getSessionType(), session.getActualMinutes(), session.getStartedAt(), session.getEndedAt()))
                .toList();
    }

    @Override
    public List<FocusSessionResponse> listRelationshipCalendarSessions(Long relationshipId, YearMonth yearMonth) {
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();
        List<FocusSession> sessions = focusSessionMapper.selectList(new LambdaQueryWrapper<FocusSession>()
                .eq(FocusSession::getSpaceId, relationshipId)
                .eq(FocusSession::getStatus, COMPLETED)
                .ge(FocusSession::getStartedAt, start)
                .lt(FocusSession::getStartedAt, end)
                .orderByAsc(FocusSession::getStartedAt));
        return sessions.stream().map(this::toSessionResponse).toList();
    }

    private FocusSettings getOrCreateSettings(Long userId) {
        FocusSettings settings = focusSettingsMapper.selectOne(new LambdaQueryWrapper<FocusSettings>()
                .eq(FocusSettings::getUserId, userId)
                .last("LIMIT 1"));
        if (settings != null) {
            return settings;
        }
        LocalDateTime now = LocalDateTime.now();
        settings = new FocusSettings();
        settings.setUserId(userId);
        settings.setFocusMinutes(DEFAULT_FOCUS_MINUTES);
        settings.setShortBreakMinutes(DEFAULT_SHORT_BREAK_MINUTES);
        settings.setLongBreakMinutes(DEFAULT_LONG_BREAK_MINUTES);
        settings.setLongBreakInterval(DEFAULT_LONG_BREAK_INTERVAL);
        settings.setAutoStartBreak(false);
        settings.setAutoStartNextFocus(false);
        settings.setSoundEnabled(true);
        settings.setNotificationEnabled(true);
        settings.setStrictModeEnabled(false);
        settings.setCreatedAt(now);
        settings.setUpdatedAt(now);
        focusSettingsMapper.insert(settings);
        return settings;
    }

    private FocusSettingsResponse toSettingsResponse(FocusSettings settings) {
        return new FocusSettingsResponse(
                settings.getId(),
                settings.getFocusMinutes(),
                settings.getShortBreakMinutes(),
                settings.getLongBreakMinutes(),
                settings.getLongBreakInterval(),
                settings.getAutoStartBreak(),
                settings.getAutoStartNextFocus(),
                settings.getSoundEnabled(),
                settings.getNotificationEnabled(),
                settings.getStrictModeEnabled(),
                settings.getCreatedAt(),
                settings.getUpdatedAt()
        );
    }

    private FocusSessionResponse toSessionResponse(FocusSession session) {
        Relationship relationship = session.getSpaceId() == null ? null : relationshipMapper.selectById(session.getSpaceId());
        LocalDateTime expectedEndAt = resolveExpectedEndAt(session);
        long remainingSeconds = expectedEndAt == null ? 0 : Math.max(0, Duration.between(LocalDateTime.now(), expectedEndAt).getSeconds());
        return new FocusSessionResponse(
                session.getId(),
                session.getUserId(),
                session.getSpaceId(),
                relationship == null ? null : relationship.getName(),
                session.getTodoId(),
                getTodoTitle(session.getTodoId()),
                session.getRoomId(),
                session.getSessionType(),
                session.getPhase(),
                session.getPlannedMinutes(),
                session.getActualMinutes(),
                session.getPausedSeconds(),
                session.getStatus(),
                session.getSource(),
                session.getNote(),
                session.getCoinsAwarded(),
                session.getCoinsAwardedAt(),
                session.getStartedAt(),
                session.getEndedAt(),
                expectedEndAt,
                remainingSeconds,
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }

    private FocusRoomResponse toRoomResponse(FocusRoom room) {
        Relationship relationship = relationshipMapper.selectById(room.getSpaceId());
        LocalDateTime expectedEndAt = room.getStartedAt() == null ? null : room.getStartedAt().plusMinutes(room.getPlannedMinutes());
        long remainingSeconds = expectedEndAt == null ? 0 : Math.max(0, Duration.between(LocalDateTime.now(), expectedEndAt).getSeconds());
        return new FocusRoomResponse(
                room.getId(),
                room.getCreatorUserId(),
                room.getSpaceId(),
                relationship == null ? null : relationship.getName(),
                room.getTitle(),
                room.getPlannedMinutes(),
                room.getStatus(),
                room.getStartedAt(),
                room.getEndedAt(),
                expectedEndAt,
                remainingSeconds,
                room.getCreatedAt(),
                room.getUpdatedAt(),
                listRoomMembers(room.getId()).stream().map(this::toRoomMemberResponse).toList()
        );
    }

    private FocusRoomResponse.FocusRoomMemberResponse toRoomMemberResponse(FocusRoomMember member) {
        User user = userMapper.selectById(member.getUserId());
        return new FocusRoomResponse.FocusRoomMemberResponse(
                member.getUserId(),
                user == null ? null : user.getUsername(),
                user == null ? null : user.getAvatarUrl(),
                member.getMemberStatus(),
                member.getJoinedAt(),
                member.getCompletedAt()
        );
    }

    private Integer resolvePlannedMinutes(Integer plannedMinutes, Long userId) {
        if (plannedMinutes != null) {
            return plannedMinutes;
        }
        return getOrCreateSettings(userId).getFocusMinutes();
    }

    private SpaceTodo requireVisibleTodo(Long todoId, Long userId) {
        SpaceTodo todo = spaceTodoMapper.selectById(todoId);
        if (todo == null || "DELETED".equals(todo.getStatus())) {
            throw new BusinessException(404, "Todo not found");
        }
        relationshipPermissionService.requireActiveRelationshipMember(todo.getRelationshipId(), userId);
        return todo;
    }

    private FocusSession requireOwnSession(Long sessionId, Long userId) {
        FocusSession session = focusSessionMapper.selectById(sessionId);
        if (session == null || !userId.equals(session.getUserId())) {
            throw new BusinessException(404, "Focus session not found");
        }
        return session;
    }

    private FocusSession findCurrentSession(Long userId) {
        return focusSessionMapper.selectOne(new LambdaQueryWrapper<FocusSession>()
                .eq(FocusSession::getUserId, userId)
                .in(FocusSession::getStatus, List.of(RUNNING, PAUSED))
                .orderByDesc(FocusSession::getStartedAt)
                .last("LIMIT 1"));
    }

    private FocusSession findRoomActiveSession(Long roomId, Long userId) {
        return focusSessionMapper.selectOne(new LambdaQueryWrapper<FocusSession>()
                .eq(FocusSession::getRoomId, roomId)
                .eq(FocusSession::getUserId, userId)
                .in(FocusSession::getStatus, List.of(RUNNING, PAUSED))
                .orderByDesc(FocusSession::getStartedAt)
                .last("LIMIT 1"));
    }

    private void expireStaleCurrent(Long userId) {
        FocusSession current = findCurrentSession(userId);
        if (current == null || PAUSED.equals(current.getStatus())) {
            return;
        }
        LocalDateTime expiredAfter = current.getStartedAt()
                .plusMinutes(current.getPlannedMinutes())
                .plusSeconds(safeInt(current.getPausedSeconds()))
                .plusHours(2);
        if (LocalDateTime.now().isAfter(expiredAfter)) {
            current.setStatus(EXPIRED);
            current.setEndedAt(LocalDateTime.now());
            current.setActualMinutes(calculateActualMinutes(current, current.getEndedAt()));
            current.setUpdatedAt(LocalDateTime.now());
            focusSessionMapper.updateById(current);
            recordEvent(current, "EXPIRE", null);
        }
    }

    private LocalDateTime resolveExpectedEndAt(FocusSession session) {
        if (session.getStartedAt() == null || session.getPlannedMinutes() == null) {
            return session.getEndedAt();
        }
        if (PAUSED.equals(session.getStatus())) {
            return LocalDateTime.now().plusSeconds(Math.max(0, session.getPlannedMinutes() * 60L - elapsedFocusSeconds(session, LocalDateTime.now())));
        }
        return session.getStartedAt().plusMinutes(session.getPlannedMinutes()).plusSeconds(safeInt(session.getPausedSeconds()));
    }

    private int currentPausedSeconds(FocusSession session) {
        int pausedSeconds = safeInt(session.getPausedSeconds());
        if (!PAUSED.equals(session.getStatus())) {
            return pausedSeconds;
        }
        FocusSessionEvent pause = focusSessionEventMapper.selectOne(new LambdaQueryWrapper<FocusSessionEvent>()
                .eq(FocusSessionEvent::getSessionId, session.getId())
                .eq(FocusSessionEvent::getEventType, "PAUSE")
                .orderByDesc(FocusSessionEvent::getEventTime)
                .last("LIMIT 1"));
        if (pause == null) {
            return pausedSeconds;
        }
        return pausedSeconds + (int) Math.max(0, Duration.between(pause.getEventTime(), LocalDateTime.now()).getSeconds());
    }

    private long elapsedFocusSeconds(FocusSession session, LocalDateTime endTime) {
        LocalDateTime effectiveEnd = endTime;
        if (PAUSED.equals(session.getStatus())) {
            FocusSessionEvent pause = focusSessionEventMapper.selectOne(new LambdaQueryWrapper<FocusSessionEvent>()
                    .eq(FocusSessionEvent::getSessionId, session.getId())
                    .eq(FocusSessionEvent::getEventType, "PAUSE")
                    .orderByDesc(FocusSessionEvent::getEventTime)
                    .last("LIMIT 1"));
            if (pause != null) {
                effectiveEnd = pause.getEventTime();
            }
        }
        return Math.max(0, Duration.between(session.getStartedAt(), effectiveEnd).getSeconds() - safeInt(session.getPausedSeconds()));
    }

    private int calculateActualMinutes(FocusSession session, LocalDateTime endTime) {
        long seconds = Math.max(0, Duration.between(session.getStartedAt(), endTime).getSeconds() - safeInt(session.getPausedSeconds()));
        if (seconds == 0) {
            return 0;
        }
        return (int) Math.max(1, (seconds + 59) / 60);
    }

    private void recordEvent(FocusSession session, String eventType, Map<String, Object> metadata) {
        FocusSessionEvent event = new FocusSessionEvent();
        event.setSessionId(session.getId());
        event.setUserId(session.getUserId());
        event.setEventType(eventType);
        event.setEventTime(LocalDateTime.now());
        event.setMetadata(writeMetadata(metadata));
        event.setCreatedAt(LocalDateTime.now());
        focusSessionEventMapper.insert(event);
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

    private String normalizePhase(String phase) {
        if (!StringUtils.hasText(phase)) {
            return FOCUS;
        }
        String value = phase.trim().toUpperCase();
        if (!List.of(FOCUS, "SHORT_BREAK", "LONG_BREAK").contains(value)) {
            throw new BusinessException(400, "Invalid focus phase");
        }
        return value;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private LocalDateTime parseStart(String value, LocalDate fallbackDate) {
        return StringUtils.hasText(value) ? LocalDate.parse(value).atStartOfDay() : fallbackDate.atStartOfDay();
    }

    private LocalDateTime parseEnd(String value, LocalDate fallbackDate) {
        return StringUtils.hasText(value) ? LocalDate.parse(value).plusDays(1).atStartOfDay() : fallbackDate.atStartOfDay();
    }

    private Integer countStatus(List<FocusSession> sessions, String status) {
        return (int) sessions.stream().filter(item -> status.equals(item.getStatus())).count();
    }

    private Integer getWeekMinutes(Long userId, LocalDate today) {
        LocalDateTime start = today.minusDays(6).atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        return focusSessionMapper.selectList(new LambdaQueryWrapper<FocusSession>()
                        .eq(FocusSession::getUserId, userId)
                        .eq(FocusSession::getStatus, COMPLETED)
                        .ge(FocusSession::getStartedAt, start)
                        .lt(FocusSession::getStartedAt, end))
                .stream().mapToInt(item -> safeInt(item.getActualMinutes())).sum();
    }

    private Integer getCurrentStreak(Long userId) {
        List<FocusSession> sessions = focusSessionMapper.selectList(new LambdaQueryWrapper<FocusSession>()
                .eq(FocusSession::getUserId, userId)
                .eq(FocusSession::getStatus, COMPLETED)
                .ge(FocusSession::getStartedAt, LocalDate.now().minusDays(30).atStartOfDay()));
        Set<LocalDate> completedDays = new LinkedHashSet<LocalDate>();
        sessions.forEach(item -> completedDays.add(item.getStartedAt().toLocalDate()));
        int streak = 0;
        LocalDate cursor = LocalDate.now();
        while (completedDays.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private List<FocusStatsResponse.TopTodoResponse> buildTopTodos(List<FocusSession> sessions) {
        Map<Long, FocusStatsResponse.TopTodoResponse> map = new HashMap<Long, FocusStatsResponse.TopTodoResponse>();
        for (FocusSession session : sessions) {
            if (!COMPLETED.equals(session.getStatus()) || session.getTodoId() == null) {
                continue;
            }
            FocusStatsResponse.TopTodoResponse item = map.computeIfAbsent(session.getTodoId(),
                    id -> new FocusStatsResponse.TopTodoResponse(id, getTodoTitle(id), 0, 0));
            item.setFocusMinutes(item.getFocusMinutes() + safeInt(session.getActualMinutes()));
            item.setSessionsCount(item.getSessionsCount() + 1);
        }
        return map.values().stream()
                .sorted(Comparator.comparing(FocusStatsResponse.TopTodoResponse::getFocusMinutes).reversed())
                .limit(5)
                .toList();
    }

    private List<FocusStatsResponse.DailyTrendResponse> buildDailyTrend(Long userId, LocalDate startDate, LocalDate endDate) {
        List<FocusSession> sessions = focusSessionMapper.selectList(new LambdaQueryWrapper<FocusSession>()
                .eq(FocusSession::getUserId, userId)
                .eq(FocusSession::getStatus, COMPLETED)
                .ge(FocusSession::getStartedAt, startDate.atStartOfDay())
                .lt(FocusSession::getStartedAt, endDate.plusDays(1).atStartOfDay()));
        List<FocusStatsResponse.DailyTrendResponse> trend = new ArrayList<FocusStatsResponse.DailyTrendResponse>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            LocalDate date = cursor;
            int minutes = sessions.stream().filter(item -> item.getStartedAt().toLocalDate().equals(date)).mapToInt(item -> safeInt(item.getActualMinutes())).sum();
            int count = (int) sessions.stream().filter(item -> item.getStartedAt().toLocalDate().equals(date)).count();
            trend.add(new FocusStatsResponse.DailyTrendResponse(date, minutes, count));
            cursor = cursor.plusDays(1);
        }
        return trend;
    }

    private String getTodoTitle(Long todoId) {
        if (todoId == null) {
            return null;
        }
        SpaceTodo todo = spaceTodoMapper.selectById(todoId);
        return todo == null ? null : todo.getTitle();
    }

    private FocusRoom requireRoom(Long roomId) {
        FocusRoom room = focusRoomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(404, "Focus room not found");
        }
        return room;
    }

    private FocusRoomMember requireRoomMember(Long roomId, Long userId) {
        FocusRoomMember member = focusRoomMemberMapper.selectOne(new LambdaQueryWrapper<FocusRoomMember>()
                .eq(FocusRoomMember::getRoomId, roomId)
                .eq(FocusRoomMember::getUserId, userId)
                .last("LIMIT 1"));
        if (member == null) {
            throw new BusinessException(403, "No permission to access this focus room");
        }
        return member;
    }

    private List<FocusRoomMember> listRoomMembers(Long roomId) {
        return focusRoomMemberMapper.selectList(new LambdaQueryWrapper<FocusRoomMember>()
                .eq(FocusRoomMember::getRoomId, roomId)
                .orderByAsc(FocusRoomMember::getId));
    }

    private void createRoomMember(Long roomId, Long userId, String status, LocalDateTime now) {
        FocusRoomMember member = new FocusRoomMember();
        member.setRoomId(roomId);
        member.setUserId(userId);
        member.setMemberStatus(status);
        member.setJoinedAt(JOINED.equals(status) ? now : null);
        member.setCreatedAt(now);
        member.setUpdatedAt(now);
        focusRoomMemberMapper.insert(member);
    }

    private void startRoomSession(FocusRoom room, Long userId, LocalDateTime now) {
        FocusSession session = new FocusSession();
        session.setUserId(userId);
        session.setSpaceId(room.getSpaceId());
        session.setRoomId(room.getId());
        session.setSessionType(COUPLE);
        session.setPhase(FOCUS);
        session.setPlannedMinutes(room.getPlannedMinutes());
        session.setActualMinutes(0);
        session.setStartedAt(now);
        session.setPausedSeconds(0);
        session.setStatus(RUNNING);
        session.setSource(MANUAL);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        focusSessionMapper.insert(session);
        recordEvent(session, "START", Map.of("roomId", room.getId()));
    }

    private void syncRoomMemberStatus(Long roomId, Long userId, String status) {
        if (roomId == null) {
            return;
        }
        FocusRoomMember member = requireRoomMember(roomId, userId);
        member.setMemberStatus(status);
        if (MEMBER_COMPLETED.equals(status)) {
            member.setCompletedAt(LocalDateTime.now());
        }
        member.setUpdatedAt(LocalDateTime.now());
        focusRoomMemberMapper.updateById(member);
    }

    private void completeRoomIfDone(Long roomId) {
        if (roomId == null) {
            return;
        }
        FocusRoom room = focusRoomMapper.selectById(roomId);
        if (room == null || ROOM_COMPLETED.equals(room.getStatus())) {
            return;
        }
        boolean hasActive = listRoomMembers(roomId).stream().anyMatch(member -> List.of(INVITED, JOINED, FOCUSING, MEMBER_PAUSED).contains(member.getMemberStatus()));
        if (!hasActive) {
            LocalDateTime now = LocalDateTime.now();
            room.setStatus(ROOM_COMPLETED);
            room.setEndedAt(now);
            room.setUpdatedAt(now);
            focusRoomMapper.updateById(room);
            for (FocusRoomMember member : listRoomMembers(roomId)) {
                createNotificationSafely(member.getUserId(), null, "FOCUS_ROOM_COMPLETED", "一起专注完成",
                        "你们刚刚一起专注了 " + room.getPlannedMinutes() + " 分钟。", "FOCUS_ROOM", room.getId(), room.getSpaceId(),
                        Map.of("plannedMinutes", room.getPlannedMinutes()));
            }
        }
    }

    private void notifySessionCompleted(FocusSession session) {
        createNotificationSafely(session.getUserId(), null, "FOCUS_SESSION_COMPLETED", "专注完成",
                "专注完成了，休息一下吧。", "FOCUS_SESSION", session.getId(), session.getSpaceId(),
                Map.of("actualMinutes", session.getActualMinutes(), "phase", session.getPhase()));
        if (session.getSpaceId() != null) {
            List<RelationshipMember> members = relationshipPermissionService.listActiveMembers(session.getSpaceId());
            for (RelationshipMember member : members) {
                createNotificationSafely(member.getUserId(), session.getUserId(), "FOCUS_PARTNER_COMPLETED", "对方完成了一次专注",
                        "对方完成了一次专注。", "FOCUS_SESSION", session.getId(), session.getSpaceId(),
                        Map.of("actualMinutes", session.getActualMinutes(), "sessionType", session.getSessionType()));
            }
        }
    }

    private void createNotificationSafely(Long receiverUserId, Long actorUserId, String notificationType, String title, String content,
                                          String relatedType, Long relatedId, Long relationshipId, Map<String, Object> metadata) {
        try {
            notificationService.createNotification(receiverUserId, actorUserId, notificationType, title, content, relatedType, relatedId, relationshipId, metadata);
        } catch (Exception ex) {
            log.warn("Create focus notification failed: {}", notificationType, ex);
        }
    }
}
