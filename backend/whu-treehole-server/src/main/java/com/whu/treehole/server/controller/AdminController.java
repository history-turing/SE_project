package com.whu.treehole.server.controller;

import com.whu.treehole.common.api.ApiResponse;
import com.whu.treehole.domain.dto.AuditLogDto;
import com.whu.treehole.domain.dto.AdminUserDto;
import com.whu.treehole.domain.dto.AnnouncementSummaryDto;
import com.whu.treehole.domain.dto.AnnouncementSaveRequest;
import com.whu.treehole.domain.dto.ReportResolveRequest;
import com.whu.treehole.domain.dto.ReportSummaryDto;
import com.whu.treehole.domain.dto.RoleAssignmentRequest;
import com.whu.treehole.domain.dto.RoleDto;
import com.whu.treehole.domain.dto.TrendingTopicAdminDto;
import com.whu.treehole.domain.dto.TrendingTopicRuleRequest;
import com.whu.treehole.domain.dto.UserBanRequest;
import com.whu.treehole.server.service.AnnouncementService;
import com.whu.treehole.server.service.ModerationService;
import com.whu.treehole.server.service.TrendingTopicService;
import com.whu.treehole.server.support.AuthContextHolder;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final ModerationService moderationService;
    private final TrendingTopicService trendingTopicService;
    private final AnnouncementService announcementService;

    public AdminController(ModerationService moderationService,
                           TrendingTopicService trendingTopicService,
                           AnnouncementService announcementService) {
        this.moderationService = moderationService;
        this.trendingTopicService = trendingTopicService;
        this.announcementService = announcementService;
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

    @PostMapping("/users/{userCode}/unban")
    public ApiResponse<Void> unbanUser(@PathVariable String userCode) {
        moderationService.unbanUser(AuthContextHolder.currentUserId(), userCode);
        return ApiResponse.success(null);
    }

    @PostMapping("/posts/{postCode}/restore")
    public ApiResponse<Void> restorePost(@PathVariable String postCode) {
        moderationService.restorePost(AuthContextHolder.currentUserId(), postCode);
        return ApiResponse.success(null);
    }

    @PostMapping("/posts/{postCode}/comments/{commentCode}/restore")
    public ApiResponse<Void> restoreComment(@PathVariable String postCode, @PathVariable String commentCode) {
        moderationService.restoreComment(AuthContextHolder.currentUserId(), postCode, commentCode);
        return ApiResponse.success(null);
    }

    @GetMapping("/reports")
    public ApiResponse<List<ReportSummaryDto>> listReports() {
        return ApiResponse.success(moderationService.listReports(AuthContextHolder.currentUserId()));
    }

    @PostMapping("/reports/{reportCode}/resolve")
    public ApiResponse<Void> resolveReport(@PathVariable String reportCode,
                                           @Valid @RequestBody ReportResolveRequest request) {
        moderationService.resolveReport(AuthContextHolder.currentUserId(), reportCode, request);
        return ApiResponse.success(null);
    }

    @GetMapping("/audit-logs")
    public ApiResponse<List<AuditLogDto>> listAuditLogs() {
        return ApiResponse.success(moderationService.listAuditLogs(AuthContextHolder.currentUserId()));
    }

    @GetMapping("/users")
    public ApiResponse<List<AdminUserDto>> listUsers() {
        return ApiResponse.success(moderationService.listUsers(AuthContextHolder.currentUserId()));
    }

    @GetMapping("/roles")
    public ApiResponse<List<RoleDto>> listRoles() {
        return ApiResponse.success(moderationService.listRoles(AuthContextHolder.currentUserId()));
    }

    @GetMapping("/trending-topics")
    public ApiResponse<List<TrendingTopicAdminDto>> listTrendingTopics() {
        return ApiResponse.success(trendingTopicService.listAdminTopics(AuthContextHolder.currentUserId()));
    }

    @PostMapping("/trending-topics/rules")
    public ApiResponse<Void> saveTrendingTopicRule(@Valid @RequestBody TrendingTopicRuleRequest request) {
        trendingTopicService.saveRule(AuthContextHolder.currentUserId(), request);
        return ApiResponse.success(null);
    }

    @GetMapping("/announcements")
    public ApiResponse<List<AnnouncementSummaryDto>> listAnnouncements() {
        return ApiResponse.success(announcementService.listAdminAnnouncements(AuthContextHolder.currentUserId()));
    }

    @PostMapping("/announcements")
    public ApiResponse<AnnouncementSummaryDto> createAnnouncement(@Valid @RequestBody AnnouncementSaveRequest request) {
        return ApiResponse.success(announcementService.createAnnouncement(AuthContextHolder.currentUserId(), request));
    }

    @PutMapping("/announcements/{announcementCode}")
    public ApiResponse<AnnouncementSummaryDto> updateAnnouncement(@PathVariable String announcementCode,
                                                                  @Valid @RequestBody AnnouncementSaveRequest request) {
        return ApiResponse.success(announcementService.updateAnnouncement(
                AuthContextHolder.currentUserId(),
                announcementCode,
                request));
    }

    @PostMapping("/announcements/{announcementCode}/publish")
    public ApiResponse<Void> publishAnnouncement(@PathVariable String announcementCode) {
        announcementService.publishAnnouncement(AuthContextHolder.currentUserId(), announcementCode);
        return ApiResponse.success(null);
    }

    @PostMapping("/announcements/{announcementCode}/offline")
    public ApiResponse<Void> offlineAnnouncement(@PathVariable String announcementCode) {
        announcementService.offlineAnnouncement(AuthContextHolder.currentUserId(), announcementCode);
        return ApiResponse.success(null);
    }
}
