package com.whu.treehole.domain.dto;

public record MessageRecipientStateDto(
        Long userId,
        ConversationListItemDto conversation,
        MessageDto message,
        UnreadNotificationDto unreadNotification
) {
}
