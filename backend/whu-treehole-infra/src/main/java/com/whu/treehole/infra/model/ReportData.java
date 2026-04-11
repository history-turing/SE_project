package com.whu.treehole.infra.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReportData {
    private Long id;
    private String reportCode;
    private String targetType;
    private String targetCode;
    private Long reporterUserId;
    private String reasonCode;
    private String reasonDetail;
    private String status;
    private Long assignedUserId;
    private String resolutionCode;
    private String resolutionNote;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
