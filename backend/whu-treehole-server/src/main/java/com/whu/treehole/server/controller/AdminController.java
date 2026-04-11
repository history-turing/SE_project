package com.whu.treehole.server.controller;

import com.whu.treehole.common.api.ApiResponse;
import com.whu.treehole.domain.dto.RoleAssignmentRequest;
import com.whu.treehole.domain.dto.UserBanRequest;
import com.whu.treehole.server.service.ModerationService;
import com.whu.treehole.server.support.AuthContextHolder;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final ModerationService moderationService;

    public AdminController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    @PostMapping("/users/{userCode}/roles")
    public ApiResponse<Void> assignRole(@PathVariable String userCode,
                                        @Valid @RequestBody RoleAssignmentRequest request) {
        moderationService.assignRole(AuthContextHolder.currentUserId(), userCode, request.roleCode());
        return ApiResponse.success(null);
    }

    @PostMapping("/users/{userCode}/ban")
    public ApiResponse<Void> banUser(@PathVariable String userCode, @Valid @RequestBody UserBanRequest request) {
        moderationService.banUser(AuthContextHolder.currentUserId(), userCode, request.reason());
        return ApiResponse.success(null);
    }
}
