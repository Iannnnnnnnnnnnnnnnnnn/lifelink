package com.lifelink.focus.service;

import com.lifelink.focus.dto.CreateFocusRoomRequest;
import com.lifelink.focus.dto.FocusCalendarEventResponse;
import com.lifelink.focus.dto.FocusRoomResponse;
import com.lifelink.focus.dto.FocusSessionResponse;
import com.lifelink.focus.dto.FocusSettingsResponse;
import com.lifelink.focus.dto.FocusStatsResponse;
import com.lifelink.focus.dto.StartFocusSessionRequest;
import com.lifelink.focus.dto.UpdateFocusSettingsRequest;

import java.time.YearMonth;
import java.util.List;

public interface FocusService {

    FocusSettingsResponse getSettings(Long userId);

    FocusSettingsResponse updateSettings(UpdateFocusSettingsRequest request, Long userId);

    FocusSessionResponse startSession(StartFocusSessionRequest request, Long userId);

    FocusSessionResponse pauseSession(Long sessionId, Long userId);

    FocusSessionResponse resumeSession(Long sessionId, Long userId);

    FocusSessionResponse completeSession(Long sessionId, Long userId);

    FocusSessionResponse abandonSession(Long sessionId, Long userId);

    FocusSessionResponse getCurrentSession(Long userId);

    List<FocusSessionResponse> listSessions(String startDate, String endDate, Long userId);

    FocusStatsResponse getStats(String range, Long userId);

    FocusRoomResponse createRoom(CreateFocusRoomRequest request, Long userId);

    FocusRoomResponse getRoom(Long roomId, Long userId);

    FocusRoomResponse joinRoom(Long roomId, Long userId);

    FocusRoomResponse declineRoom(Long roomId, Long userId);

    FocusRoomResponse startRoom(Long roomId, Long userId);

    FocusRoomResponse completeRoom(Long roomId, Long userId);

    FocusRoomResponse abandonRoom(Long roomId, Long userId);

    FocusRoomResponse getCurrentRoom(Long userId);

    List<FocusRoomResponse> listRooms(Long spaceId, Long userId);

    List<FocusCalendarEventResponse> getCalendarEvents(Integer year, Integer month, Long userId);

    List<FocusSessionResponse> listRelationshipCalendarSessions(Long relationshipId, YearMonth yearMonth);
}
