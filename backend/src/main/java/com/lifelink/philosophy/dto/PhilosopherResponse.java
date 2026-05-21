package com.lifelink.philosophy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhilosopherResponse {

    private String code;

    private String name;

    private String nameZh;

    private String nameEn;

    private String era;

    private String eraZh;

    private String eraEn;

    private String description;

    private String descriptionZh;

    private String descriptionEn;

    private String avatarUrl;

    private List<String> tags;

    private Integer sortOrder;
}
