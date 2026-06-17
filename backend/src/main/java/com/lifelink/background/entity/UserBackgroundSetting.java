package com.lifelink.background.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_background_settings")
public class UserBackgroundSetting {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Boolean enabled;

    private String objectKey;

    private String imageUrl;

    private Double scale;

    private Integer positionX;

    private Integer positionY;

    private String presetPosition;

    private Double opacity;

    private Integer blur;

    private Double overlayOpacity;

    private String scope;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
