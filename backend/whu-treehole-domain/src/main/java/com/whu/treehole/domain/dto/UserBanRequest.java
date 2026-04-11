package com.whu.treehole.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record UserBanRequest(
        @NotBlank(message = "封禁原因不能为空")
        String reason
) {
}
