package com.lifelink.cycle.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleCareAdvice {

    private String title;

    private String reminder;

    private String clothingAdvice;

    private String foodAdvice;

    private String restAdvice;

    private String moodAdvice;

    private String partnerAdvice;
}
