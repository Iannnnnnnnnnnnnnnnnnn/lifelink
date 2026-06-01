package com.lifelink.focus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FocusRoomResponse {

    private Long id;

    private Long creatorUserId;

    private Long spaceId;

    private String spaceName;

    private String title;

    private Integer plannedMinutes;

    private String status;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private LocalDateTime expectedEndAt;

    private Long remainingSeconds;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<FocusRoomMemberResponse> members;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FocusRoomMemberResponse {

        private Long userId;

        private String username;

        private String avatarUrl;

        private String memberStatus;

        private LocalDateTime joinedAt;

        private LocalDateTime completedAt;
    }
}
