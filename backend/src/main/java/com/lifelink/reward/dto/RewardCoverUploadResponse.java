package com.lifelink.reward.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardCoverUploadResponse {

    private String coverUrl;

    private String objectKey;
}
