package com.whu.treehole.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record DirectConversationRequest(
        @NotBlank(message = "绉佷俊鐩爣涓嶈兘涓虹┖")
        String peerUserCode,
        String sourcePostCode,
        boolean anonymousEntry
) {
}
