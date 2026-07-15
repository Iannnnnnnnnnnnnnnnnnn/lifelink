package com.lifelink.dating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatingRecordListResponse {

    private Integer total;
    private List<DatingRecordResponse> records;
}
