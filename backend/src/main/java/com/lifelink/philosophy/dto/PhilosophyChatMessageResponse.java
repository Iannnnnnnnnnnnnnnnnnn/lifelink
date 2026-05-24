package com.lifelink.philosophy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhilosophyChatMessageResponse {

    private Long id;

    private String role;

    private String content;

    private LocalDateTime createdAt;
}
