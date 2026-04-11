package com.whu.treehole.server.controller;

/* 帖子控制器负责发帖、点赞和收藏接口。 */

import com.whu.treehole.common.api.ApiResponse;
import com.whu.treehole.domain.dto.CommentCreateRequest;
import com.whu.treehole.domain.dto.PostCardDto;
import com.whu.treehole.domain.dto.PostCommentDto;
import com.whu.treehole.domain.dto.PostCommentsDto;
import com.whu.treehole.domain.dto.PostCreateRequest;
import com.whu.treehole.domain.dto.ToggleResponse;
import com.whu.treehole.server.service.PostCommentService;
import com.whu.treehole.server.service.PostCommandService;
import com.whu.treehole.server.support.AuthContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostCommandService postCommandService;
    private final PostCommentService postCommentService;

    public PostController(PostCommandService postCommandService, PostCommentService postCommentService) {
        this.postCommandService = postCommandService;
        this.postCommentService = postCommentService;
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

    @DeleteMapping("/{postCode}")
    public ApiResponse<Void> deletePost(@PathVariable String postCode) {
        postCommandService.deletePost(AuthContextHolder.currentUserId(), postCode);
        return ApiResponse.success(null);
    }

    @GetMapping("/{postCode}/comments")
    public ApiResponse<PostCommentsDto> listComments(@PathVariable String postCode) {
        return ApiResponse.success(postCommentService.listComments(AuthContextHolder.currentUserId(), postCode));
    }

    @PostMapping("/{postCode}/comments")
    public ApiResponse<PostCommentDto> createComment(@PathVariable String postCode,
                                                     @Valid @RequestBody CommentCreateRequest request) {
        return ApiResponse.success(postCommentService.createComment(
                AuthContextHolder.currentUserId(), postCode, request));
    }

    @PostMapping("/{postCode}/comments/{commentCode}/replies")
    public ApiResponse<PostCommentDto> replyComment(@PathVariable String postCode,
                                                    @PathVariable String commentCode,
                                                    @Valid @RequestBody CommentCreateRequest request) {
        return ApiResponse.success(postCommentService.replyComment(
                AuthContextHolder.currentUserId(), postCode, commentCode, request));
    }

    @DeleteMapping("/{postCode}/comments/{commentCode}")
    public ApiResponse<Void> deleteComment(@PathVariable String postCode, @PathVariable String commentCode) {
        postCommentService.deleteComment(AuthContextHolder.currentUserId(), postCode, commentCode);
        return ApiResponse.success(null);
    }
}
