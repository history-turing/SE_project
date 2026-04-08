package com.whu.treehole.server.controller;

/* 页面控制器对外提供首页、话题页、校友圈和个人页聚合接口。 */

import com.whu.treehole.common.api.ApiResponse;
import com.whu.treehole.domain.dto.AlumniPageDto;
import com.whu.treehole.domain.dto.ConversationDto;
import com.whu.treehole.domain.dto.HomePageDto;
import com.whu.treehole.domain.dto.ProfilePageDto;
import com.whu.treehole.domain.dto.TopicsPageDto;
import com.whu.treehole.domain.enums.TopicScope;
import com.whu.treehole.server.service.PageQueryService;
import com.whu.treehole.server.support.AuthContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PageController {

    private final PageQueryService pageQueryService;

    public PageController(PageQueryService pageQueryService) {
        this.pageQueryService = pageQueryService;
    }

    @GetMapping("/pages/home")
    public ApiResponse<HomePageDto> getHomePage(@RequestParam(required = false) String topic,
                                                @RequestParam(required = false) String keyword) {
        return ApiResponse.success(pageQueryService.getHomePage(AuthContextHolder.currentUserId(), topic, keyword));
    }

    @GetMapping("/pages/topics")
    public ApiResponse<TopicsPageDto> getTopicsPage(@RequestParam(defaultValue = "ALL") String scope) {
        return ApiResponse.success(pageQueryService.getTopicsPage(TopicScope.from(scope)));
    }

    @GetMapping("/pages/alumni")
    public ApiResponse<AlumniPageDto> getAlumniPage(@RequestParam(required = false) String topic,
                                                    @RequestParam(required = false) String keyword) {
        return ApiResponse.success(pageQueryService.getAlumniPage(AuthContextHolder.currentUserId(), topic, keyword));
    }

    @GetMapping("/pages/profile")
    public ApiResponse<ProfilePageDto> getProfilePage(@RequestParam(required = false) String conversationCode) {
        return ApiResponse.success(pageQueryService.getProfilePage(AuthContextHolder.currentUserId(), conversationCode));
    }

    @GetMapping("/conversations/{conversationCode}")
    public ApiResponse<ConversationDto> getConversation(@PathVariable String conversationCode) {
        return ApiResponse.success(pageQueryService.getConversation(AuthContextHolder.currentUserId(), conversationCode));
    }
}
