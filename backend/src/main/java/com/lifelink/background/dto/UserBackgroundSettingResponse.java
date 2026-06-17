package com.lifelink.background.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBackgroundSettingResponse {

    private Boolean enabled;

    private String imageUrl;

    private String objectKey;

    private Double scale;

    private Integer positionX;

    private Integer positionY;

    private String presetPosition;

    private Double opacity;

    private Integer blur;

    private Double overlayOpacity;

    private String scope;
}
