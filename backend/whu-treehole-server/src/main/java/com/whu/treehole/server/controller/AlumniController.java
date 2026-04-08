package com.whu.treehole.server.controller;

/* 校友控制器负责联系人关注切换接口。 */

import com.whu.treehole.common.api.ApiResponse;
import com.whu.treehole.domain.dto.ToggleResponse;
import com.whu.treehole.server.service.AlumniCommandService;
import com.whu.treehole.server.support.AuthContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alumni")
public class AlumniController {

    private final AlumniCommandService alumniCommandService;

    public AlumniController(AlumniCommandService alumniCommandService) {
        this.alumniCommandService = alumniCommandService;
    }

    @PostMapping("/contacts/{contactCode}/follow/toggle")
    public ApiResponse<ToggleResponse> toggleFollow(@PathVariable String contactCode) {
        return ApiResponse.success(alumniCommandService.toggleFollow(AuthContextHolder.currentUserId(), contactCode));
    }
}
