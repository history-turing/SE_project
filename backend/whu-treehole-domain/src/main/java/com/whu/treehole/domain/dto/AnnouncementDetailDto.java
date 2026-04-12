package com.whu.treehole.domain.dto;

public record AnnouncementDetailDto(
        String code,
        String title,
        String summary,
        String content,
        String category,
        boolean pinned,
        boolean popupEnabled,
        boolean popupOncePerSession,
        String status,
        String publishedAt,
        String expireAt
) {
}
