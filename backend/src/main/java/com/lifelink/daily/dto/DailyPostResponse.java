package com.lifelink.daily.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyPostResponse {

    private Long id;

    private Long relationshipId;

    private String relationshipName;

    private Long userId;

    private String username;

    private String content;

    private String mood;

    private String visibility;

    private LocalDateTime createdAt;

    private List<DailyPostImageResponse> images;
}
