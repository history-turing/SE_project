package com.whu.treehole.infra.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AuditLogData {
    private Long id;
    private Long actorUserId;
    private String actorRoleSnapshot;
    private String actionType;
    private String targetType;
    private Long targetId;
    private String targetCode;
    private LocalDateTime createdAt;
}
