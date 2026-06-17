package com.lifelink.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFeaturesResponse {

    private Boolean philosophyEnabled;

    private Boolean rewardAdmin;
}
