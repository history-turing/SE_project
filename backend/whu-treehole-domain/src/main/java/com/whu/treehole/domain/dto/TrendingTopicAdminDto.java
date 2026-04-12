package com.whu.treehole.domain.dto;

public record TrendingTopicAdminDto(
        String topicKey,
        String displayName,
        String mergeTargetKey,
        boolean hidden,
        boolean pinned,
        int sortOrder,
        int postCount,
        int interactionCount,
        int uniqueAuthorCount,
        int score
) {
}
