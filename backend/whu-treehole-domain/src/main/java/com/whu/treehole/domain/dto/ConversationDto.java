package com.whu.treehole.domain.dto;

import java.util.List;

/* 会话 DTO 同时包含摘要信息和消息明细。 */

public record ConversationDto(
        String id,
        String name,
        String subtitle,
        String avatar,
        String lastMessage,
        String time,
        Integer unreadCount,
        List<MessageDto> messages
) {
}
