package com.whu.treehole.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageReportRequest(
        @NotBlank(message = "messageCode required")
        String messageCode,
        @NotBlank(message = "reason required")
        @Size(max = 255, message = "reason too long")
        String reason
) {
}
