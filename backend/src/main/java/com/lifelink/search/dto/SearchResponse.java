package com.lifelink.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {

    private String keyword;

    private Integer totalCount;

    private List<SearchGroupResponse> groups;
}
