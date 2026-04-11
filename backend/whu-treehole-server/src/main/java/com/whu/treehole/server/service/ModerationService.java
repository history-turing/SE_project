package com.whu.treehole.server.service;

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.enums.AccountStatus;
import com.whu.treehole.infra.mapper.AuthMapper;
import com.whu.treehole.infra.model.RoleData;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class ModerationService {

    private final AuthMapper authMapper;
    private final AuthorizationService authorizationService;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public ModerationService(AuthMapper authMapper,
                             AuthorizationService authorizationService,
                             AuditLogService auditLogService,
                             Clock clock) {
        this.authMapper = authMapper;
        this.authorizationService = authorizationService;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    public void assignRole(long actorUserId, String userCode, String roleCode) {
        authorizationService.assertCanWrite(actorUserId, "role.assign.admin");

        Long targetUserId = requireUserId(userCode);
        RoleData role = requireRole(roleCode);
        List<RoleData> actorRoles = authMapper.selectRolesByUserId(actorUserId);
        List<RoleData> targetRoles = authMapper.selectRolesByUserId(targetUserId);

        if (!hasRole(actorRoles, "SUPER_ADMIN")) {
            throw new BusinessException(4030, "FORBIDDEN");
        }
        if (!Objects.equals("ADMIN", role.getCode())) {
            throw new BusinessException(4030, "FORBIDDEN");
        }
        if (hasRole(targetRoles, "SUPER_ADMIN")) {
            throw new BusinessException(4030, "FORBIDDEN");
        }

        authMapper.insertUserRole(targetUserId, role.getId(), LocalDateTime.now(clock), actorUserId);
        auditLogService.record("ASSIGN_ROLE", actorUserId, "USER", targetUserId, userCode);
    }

    public void banUser(long actorUserId, String userCode, String reason) {
        authorizationService.assertCanWrite(actorUserId, "user.ban");

        Long targetUserId = requireUserId(userCode);
        List<RoleData> actorRoles = authMapper.selectRolesByUserId(actorUserId);
        List<RoleData> targetRoles = authMapper.selectRolesByUserId(targetUserId);

        if (hasRole(targetRoles, "SUPER_ADMIN")) {
            throw new BusinessException(4030, "FORBIDDEN");
        }
        if (hasRole(targetRoles, "ADMIN") && !hasRole(actorRoles, "SUPER_ADMIN")) {
            throw new BusinessException(4030, "FORBIDDEN");
        }

        authMapper.updateAccountStatus(
                targetUserId,
                AccountStatus.BANNED.name(),
                reason == null ? "" : reason.trim(),
                LocalDateTime.now(clock),
                actorUserId);
        auditLogService.record("BAN_USER", actorUserId, "USER", targetUserId, userCode);
    }

    private Long requireUserId(String userCode) {
        Long userId = authMapper.selectUserIdByUserCode(userCode);
        if (userId == null) {
            throw new BusinessException(4040, "用户不存在");
        }
        return userId;
    }

    private RoleData requireRole(String roleCode) {
        RoleData roleData = authMapper.selectRoleByCode(roleCode);
        if (roleData == null) {
            throw new BusinessException(4040, "角色不存在");
        }
        return roleData;
    }

    private boolean hasRole(List<RoleData> roles, String roleCode) {
        return roles.stream().anyMatch(role -> roleCode.equals(role.getCode()));
    }
}
