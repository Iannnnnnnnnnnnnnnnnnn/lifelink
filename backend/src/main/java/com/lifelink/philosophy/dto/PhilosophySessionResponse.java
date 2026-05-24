package com.lifelink.philosophy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhilosophySessionResponse {

    private Long id;

    private String question;

    private String language;

    private List<PhilosophyResponseItem> responses;

    private LocalDateTime createdAt;
}
