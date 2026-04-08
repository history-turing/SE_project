package com.whu.treehole.server.controller;

/* 帖子控制器负责发帖、点赞和收藏接口。 */

import com.whu.treehole.common.api.ApiResponse;
import com.whu.treehole.domain.dto.PostCardDto;
import com.whu.treehole.domain.dto.PostCreateRequest;
import com.whu.treehole.domain.dto.ToggleResponse;
import com.whu.treehole.server.service.PostCommandService;
import com.whu.treehole.server.support.AuthContextHolder;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostCommandService postCommandService;

    public PostController(PostCommandService postCommandService) {
        this.postCommandService = postCommandService;
    }

    @PostMapping
    public ApiResponse<PostCardDto> createPost(@Valid @RequestBody PostCreateRequest request) {
        return ApiResponse.success(postCommandService.createPost(AuthContextHolder.currentUserId(), request));
    }

    @PostMapping("/{postCode}/likes/toggle")
    public ApiResponse<ToggleResponse> toggleLike(@PathVariable String postCode) {
        return ApiResponse.success(postCommandService.toggleLike(AuthContextHolder.currentUserId(), postCode));
    }

    @PostMapping("/{postCode}/saves/toggle")
    public ApiResponse<ToggleResponse> toggleSave(@PathVariable String postCode) {
        return ApiResponse.success(postCommandService.toggleSave(AuthContextHolder.currentUserId(), postCode));
    }
}
