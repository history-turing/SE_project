package com.whu.treehole.domain.dto;

public record AnnouncementSummaryDto(
        String code,
        String title,
        String summary,
        String category,
        boolean pinned,
        boolean popupEnabled,
        boolean popupOncePerSession,
        String status,
        String publishedAt,
        String expireAt
) {
}
