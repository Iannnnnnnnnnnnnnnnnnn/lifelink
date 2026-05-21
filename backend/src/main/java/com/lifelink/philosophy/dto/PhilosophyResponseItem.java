package com.lifelink.philosophy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhilosophyResponseItem {

    private String philosopherCode;

    private String philosopherName;

    private String viewpoint;

    private String questionBack;

    private String objection;

    private String summary;

    private String rawResponse;
}
