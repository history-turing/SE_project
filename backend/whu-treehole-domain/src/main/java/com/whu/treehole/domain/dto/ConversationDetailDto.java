package com.whu.treehole.domain.dto;

import com.whu.treehole.domain.enums.ConversationStatus;
import java.util.List;

public record ConversationDetailDto(
        String conversationCode,
        ConversationStatus status,
        ConversationPeerDto peer,
        String lastMessage,
        String lastMessageTime,
        Integer unreadCount,
        List<MessageDto> messages
) {
}
