package com.lifelink.daily.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyPostDetailResponse {

    private Long id;

    private Long relationshipId;

    private String relationshipName;

    private Long userId;

    private String username;

    private String content;

    private String mood;

    private String visibility;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<DailyPostImageResponse> images;
}
