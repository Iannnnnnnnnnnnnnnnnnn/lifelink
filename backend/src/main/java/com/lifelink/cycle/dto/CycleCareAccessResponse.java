package com.lifelink.cycle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleCareAccessResponse {

    private Boolean enabled;

    private String reason;

    private List<Long> loverSpaceIds;
}
