package com.lifelink.background.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBackgroundUploadResponse {

    private String imageUrl;

    private String objectKey;
}
