package com.lifelink.focus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FocusSessionResponse {

    private Long sessionId;

    private Long userId;

    private Long spaceId;

    private String spaceName;

    private Long todoId;

    private String todoTitle;

    private Long roomId;

    private String sessionType;

    private String phase;

    private Integer plannedMinutes;

    private Integer actualMinutes;

    private Integer pausedSeconds;

    private String status;

    private String source;

    private String note;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private LocalDateTime expectedEndAt;

    private Long remainingSeconds;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
