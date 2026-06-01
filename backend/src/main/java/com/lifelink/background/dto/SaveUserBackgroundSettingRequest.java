package com.lifelink.background.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class SaveUserBackgroundSettingRequest {

    private Boolean enabled;

    private String objectKey;

    @DecimalMin("0.5")
    @DecimalMax("3.0")
    private Double scale;

    @Min(0)
    @Max(100)
    private Integer positionX;

    @Min(0)
    @Max(100)
    private Integer positionY;

    private String presetPosition;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double opacity;

    @Min(0)
    @Max(30)
    private Integer blur;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double overlayOpacity;

    private String scope;
}
