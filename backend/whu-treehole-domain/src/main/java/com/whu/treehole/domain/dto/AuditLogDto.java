package com.whu.treehole.domain.dto;

public record AuditLogDto(
        Long id,
        Long actorUserId,
        String actorRoleSnapshot,
        String actionType,
        String targetType,
        Long targetId,
        String targetCode,
        String createdAt
) {
}
