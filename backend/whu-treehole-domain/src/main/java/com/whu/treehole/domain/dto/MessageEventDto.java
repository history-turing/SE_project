package com.whu.treehole.domain.dto;

import java.util.List;

public record MessageEventDto(
        String type,
        String conversationCode,
        String messageCode,
        Long senderUserId,
        List<Long> targetUserIds
) {
}
