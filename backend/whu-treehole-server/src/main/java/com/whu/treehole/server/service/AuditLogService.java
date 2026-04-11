package com.whu.treehole.server.service;

import com.whu.treehole.infra.mapper.AuthMapper;
import com.whu.treehole.infra.mapper.PortalCommandMapper;
import com.whu.treehole.infra.model.AuditLogData;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final PortalCommandMapper portalCommandMapper;
    private final AuthMapper authMapper;
    private final Clock clock;

    public AuditLogService(PortalCommandMapper portalCommandMapper, AuthMapper authMapper, Clock clock) {
        this.portalCommandMapper = portalCommandMapper;
        this.authMapper = authMapper;
        this.clock = clock;
    }

    public void record(String actionType, long actorUserId, String targetType, Long targetId, String targetCode) {
        AuditLogData auditLogData = new AuditLogData();
        auditLogData.setActorUserId(actorUserId);
        auditLogData.setActorRoleSnapshot(authMapper.selectRolesByUserId(actorUserId).stream()
                .map(role -> role.getCode())
                .sorted()
                .collect(Collectors.joining(",")));
        auditLogData.setActionType(actionType);
        auditLogData.setTargetType(targetType);
        auditLogData.setTargetId(targetId);
        auditLogData.setTargetCode(targetCode);
        auditLogData.setCreatedAt(LocalDateTime.now(clock));
        portalCommandMapper.insertAuditLog(auditLogData);
    }
}
