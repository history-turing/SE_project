package com.whu.treehole.server.controller;

import com.whu.treehole.common.api.ApiResponse;
import com.whu.treehole.domain.dto.SearchResultDto;
import com.whu.treehole.server.service.SearchQueryService;
import com.whu.treehole.server.support.AuthContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchQueryService searchQueryService;

    public SearchController(SearchQueryService searchQueryService) {
        this.searchQueryService = searchQueryService;
    }

    @GetMapping
    public ApiResponse<SearchResultDto> search(@RequestParam("q") String keyword) {
        return ApiResponse.success(searchQueryService.search(AuthContextHolder.currentUserId(), keyword));
    }
}
