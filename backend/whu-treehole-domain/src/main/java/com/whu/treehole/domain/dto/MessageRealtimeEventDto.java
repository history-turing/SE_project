package com.whu.treehole.domain.dto;

import java.util.List;

public record MessageRealtimeEventDto(
        String type,
        String conversationCode,
        List<MessageRecipientStateDto> recipientStates
) {
}
