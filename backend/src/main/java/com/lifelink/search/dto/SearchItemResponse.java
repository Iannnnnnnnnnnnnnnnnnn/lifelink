package com.lifelink.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchItemResponse {

    private Long id;

    private String type;

    private String title;

    private String description;

    private String highlight;

    private Long relationshipId;

    private String relationshipName;

    private String targetUrl;

    private LocalDateTime createdAt;

    private Map<String, Object> metadata;
}
