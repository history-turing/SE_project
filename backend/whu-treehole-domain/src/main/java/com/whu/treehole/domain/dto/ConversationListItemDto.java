package com.whu.treehole.domain.dto;

public record ConversationListItemDto(
        String conversationCode,
        String conversationType,
        String peerName,
        String peerSubtitle,
        String peerAvatarUrl,
        String lastMessage,
        String displayTime,
        Integer unreadCount
) {
}
