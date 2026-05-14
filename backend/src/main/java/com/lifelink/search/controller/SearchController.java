package com.lifelink.search.controller;

import com.lifelink.common.Result;
import com.lifelink.search.dto.SearchResponse;
import com.lifelink.search.service.SearchService;
import com.lifelink.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public Result<SearchResponse> search(
            @RequestParam String keyword,
            @RequestParam(required = false) String types,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        return Result.success(searchService.search(keyword, types, loginUser.getId(), page, size));
    }
}
