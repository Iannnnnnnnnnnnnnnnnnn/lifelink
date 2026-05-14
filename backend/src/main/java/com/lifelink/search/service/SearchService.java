package com.lifelink.search.service;

import com.lifelink.search.dto.SearchResponse;

public interface SearchService {

    SearchResponse search(String keyword, String types, Long userId, Integer page, Integer size);
}
