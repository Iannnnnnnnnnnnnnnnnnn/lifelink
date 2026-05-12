package com.lifelink.daily.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyPostInteractionResponse {

    private Long dailyPostId;
    private Long likeCount;
    private Long commentCount;
    private Boolean likedByMe;
}
