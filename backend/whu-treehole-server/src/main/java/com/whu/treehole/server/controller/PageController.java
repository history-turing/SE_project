package com.whu.treehole.server.controller;

/* 页面控制器对外提供首页、话题页、校友圈和个人页聚合接口。 */

import com.whu.treehole.common.api.ApiResponse;
import com.whu.treehole.domain.dto.AlumniPageDto;
import com.whu.treehole.domain.dto.AnnouncementDetailDto;
import com.whu.treehole.domain.dto.AnnouncementPopupDto;
import com.whu.treehole.domain.dto.AnnouncementSummaryDto;
import com.whu.treehole.domain.dto.ConversationDto;
import com.whu.treehole.domain.dto.HomePageDto;
import com.whu.treehole.domain.dto.ProfilePageDto;
import com.whu.treehole.domain.dto.RankingItemDto;
import com.whu.treehole.domain.dto.TopicsPageDto;
import com.whu.treehole.domain.enums.TopicScope;
import com.whu.treehole.server.service.AnnouncementService;
import com.whu.treehole.server.service.PageQueryService;
import com.whu.treehole.server.service.TrendingTopicService;
import com.whu.treehole.server.support.AuthContextHolder;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PageController {

    private final PageQueryService pageQueryService;
    private final TrendingTopicService trendingTopicService;
    private final AnnouncementService announcementService;

    public PageController(PageQueryService pageQueryService,
                          TrendingTopicService trendingTopicService,
                          AnnouncementService announcementService) {
        this.pageQueryService = pageQueryService;
        this.trendingTopicService = trendingTopicService;
        this.announcementService = announcementService;
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

    @GetMapping("/topics/trending")
    public ApiResponse<List<RankingItemDto>> getTrendingTopics() {
        return ApiResponse.success(trendingTopicService.listAllRankings());
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

    @GetMapping("/announcements")
    public ApiResponse<List<AnnouncementSummaryDto>> getAnnouncements() {
        return ApiResponse.success(announcementService.listPublishedAnnouncements());
    }

    @GetMapping("/announcements/popup")
    public ApiResponse<AnnouncementPopupDto> getPopupAnnouncement() {
        return ApiResponse.success(announcementService.getActivePopupAnnouncement());
    }

    @GetMapping("/announcements/{announcementCode}")
    public ApiResponse<AnnouncementDetailDto> getAnnouncementDetail(@PathVariable String announcementCode) {
        return ApiResponse.success(announcementService.getAnnouncementDetail(announcementCode));
    }

    @GetMapping("/conversations/{conversationCode}")
    public ApiResponse<ConversationDto> getConversation(@PathVariable String conversationCode) {
        return ApiResponse.success(pageQueryService.getConversation(AuthContextHolder.currentUserId(), conversationCode));
    }
}
