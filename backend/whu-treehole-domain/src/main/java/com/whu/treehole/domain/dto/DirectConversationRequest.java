package com.whu.treehole.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record DirectConversationRequest(
        @NotBlank(message = "私信目标不能为空")
        String peerUserCode
) {
}
