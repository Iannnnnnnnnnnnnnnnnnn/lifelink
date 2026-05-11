package com.lifelink.daily.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyPostImageResponse {

    private Long fileId;

    private String url;

    private String originalName;

    private Integer sortOrder;
}
