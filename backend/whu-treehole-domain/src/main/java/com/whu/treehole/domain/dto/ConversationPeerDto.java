package com.whu.treehole.domain.dto;

public record ConversationPeerDto(
        String userCode,
        String name,
        String subtitle,
        String avatarUrl
) {
}
