package com.whu.treehole.domain.dto;

import com.whu.treehole.domain.enums.ReportStatus;
import com.whu.treehole.domain.enums.ReportTargetType;

public record ReportSummaryDto(
        String reportCode,
        ReportTargetType targetType,
        String targetCode,
        String reasonCode,
        String reasonDetail,
        ReportStatus status,
        String resolutionCode,
        String resolutionNote,
        String createdAt
) {
}
