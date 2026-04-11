package com.whu.treehole.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record ReportResolveRequest(
        @NotBlank(message = "处理结果不能为空")
        String resolutionCode,
        String resolutionNote
) {
}
