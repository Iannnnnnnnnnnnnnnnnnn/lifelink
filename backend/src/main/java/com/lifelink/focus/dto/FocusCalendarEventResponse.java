package com.lifelink.focus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FocusCalendarEventResponse {

    private Long sessionId;

    private Long spaceId;

    private Long todoId;

    private String todoTitle;

    private Long roomId;

    private String sessionType;

    private Integer actualMinutes;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;
}
