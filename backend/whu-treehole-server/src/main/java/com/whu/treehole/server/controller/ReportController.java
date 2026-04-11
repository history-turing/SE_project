package com.whu.treehole.server.controller;

import com.whu.treehole.common.api.ApiResponse;
import com.whu.treehole.domain.dto.ReportCreateRequest;
import com.whu.treehole.domain.dto.ReportSummaryDto;
import com.whu.treehole.server.service.ModerationService;
import com.whu.treehole.server.support.AuthContextHolder;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ModerationService moderationService;

    public ReportController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    @PostMapping
    public ApiResponse<ReportSummaryDto> createReport(@Valid @RequestBody ReportCreateRequest request) {
        return ApiResponse.success(moderationService.createReport(AuthContextHolder.currentUserId(), request));
    }
}
