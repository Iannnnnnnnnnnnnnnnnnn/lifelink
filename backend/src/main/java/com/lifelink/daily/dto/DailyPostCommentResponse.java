package com.lifelink.daily.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyPostCommentResponse {

    private Long id;
    private Long dailyPostId;
    private Long userId;
    private String username;
    private String avatarUrl;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean canDelete;
}
