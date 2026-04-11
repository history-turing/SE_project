package com.whu.treehole.domain.dto;

import com.whu.treehole.domain.enums.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportCreateRequest(
        @NotNull(message = "举报目标类型不能为空")
        ReportTargetType targetType,
        @NotBlank(message = "举报目标编码不能为空")
        String targetCode,
        @NotBlank(message = "举报原因不能为空")
        String reasonCode,
        String reasonDetail
) {
}
