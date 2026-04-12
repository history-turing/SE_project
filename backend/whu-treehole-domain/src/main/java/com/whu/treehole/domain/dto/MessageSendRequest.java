package com.whu.treehole.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageSendRequest(
        @NotBlank(message = "clientMessageId required")
        @Size(max = 64, message = "clientMessageId too long")
        String clientMessageId,
        @NotBlank(message = "content required")
        @Size(max = 1000, message = "content too long")
        String content
) {
}
