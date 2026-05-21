package com.lifelink.philosophy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhilosophyChatSessionResponse {

    private Long id;

    private String philosopherCode;

    private String philosopherName;

    private String title;

    private String language;

    private String lastMessagePreview;

    private LocalDateTime lastMessageAt;

    private Integer messageCount;

    private List<PhilosophyChatMessageResponse> messages;

    private LocalDateTime createdAt;
}
