package com.whu.treehole.server.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.whu.treehole.domain.enums.AccountStatus;
import com.whu.treehole.infra.mapper.AuthMapper;
import com.whu.treehole.infra.model.PermissionData;
import com.whu.treehole.infra.model.RoleData;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ModerationServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-04-11T03:00:00Z"),
            ZoneId.of("Asia/Shanghai"));

    @Mock
    private AuthMapper authMapper;

    @Mock
    private AuditLogService auditLogService;

    private ModerationService moderationService;

    @BeforeEach
    void setUp() {
        moderationService = new ModerationService(
                authMapper,
                new AuthorizationService(authMapper),
                auditLogService,
                FIXED_CLOCK);
    }

    @Test
    void shouldAllowSuperAdminToAssignAdminRoleAndBanNormalUser() {
        PermissionData assignAdmin = new PermissionData();
        assignAdmin.setCode("role.assign.admin");
        PermissionData banUser = new PermissionData();
        banUser.setCode("user.ban");

        RoleData superAdmin = new RoleData();
        superAdmin.setId(1L);
        superAdmin.setCode("SUPER_ADMIN");

        RoleData userRole = new RoleData();
        userRole.setId(3L);
        userRole.setCode("USER");

        RoleData adminRole = new RoleData();
        adminRole.setId(2L);
        adminRole.setCode("ADMIN");

        when(authMapper.selectAccountStatusByUserId(7L)).thenReturn(AccountStatus.ACTIVE.name());
        when(authMapper.selectPermissionsByUserId(7L)).thenReturn(List.of(assignAdmin, banUser));
        when(authMapper.selectRolesByUserId(7L)).thenReturn(List.of(superAdmin));
        when(authMapper.selectUserIdByUserCode("user-12")).thenReturn(12L);
        when(authMapper.selectRolesByUserId(12L)).thenReturn(List.of(userRole));
        when(authMapper.selectRoleByCode("ADMIN")).thenReturn(adminRole);

        moderationService.assignRole(7L, "user-12", "ADMIN");
        moderationService.banUser(7L, "user-12", "spam");

        verify(authMapper).insertUserRole(eq(12L), eq(2L), any(), eq(7L));
        verify(authMapper).updateAccountStatus(eq(12L), eq("BANNED"), eq("spam"), any(), eq(7L));
        verify(auditLogService).record("ASSIGN_ROLE", 7L, "USER", 12L, "user-12");
        verify(auditLogService).record("BAN_USER", 7L, "USER", 12L, "user-12");
    }
}
