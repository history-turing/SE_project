package com.whu.treehole.domain.dto;

public record AnnouncementPopupDto(
        String code,
        String title,
        String content,
        boolean popupOncePerSession
) {
}
