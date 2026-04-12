package com.whu.treehole.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record TrendingTopicRuleRequest(
        @NotBlank(message = "话题键不能为空")
        String topicKey,
        String displayName,
        String mergeTargetKey,
        Boolean hidden,
        Boolean pinned,
        Integer sortOrder
) {
}
