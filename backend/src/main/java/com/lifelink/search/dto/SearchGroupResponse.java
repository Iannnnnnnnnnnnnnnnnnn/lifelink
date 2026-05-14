package com.lifelink.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchGroupResponse {

    private String type;

    private String title;

    private Integer count;

    private List<SearchItemResponse> items;
}
