package com.whu.treehole.domain.dto;

public record UnreadNotificationDto(
        Long userId,
        Integer messagesUnread,
        Integer interactionsUnread,
        Integer systemUnread,
        Integer totalUnread
) {
}
