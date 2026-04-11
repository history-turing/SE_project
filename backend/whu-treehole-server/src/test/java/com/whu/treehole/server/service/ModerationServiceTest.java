package com.whu.treehole.server.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.whu.treehole.domain.dto.ReportCreateRequest;
import com.whu.treehole.domain.dto.ReportResolveRequest;
import com.whu.treehole.domain.dto.ReportSummaryDto;
import com.whu.treehole.domain.enums.ReportStatus;
import com.whu.treehole.domain.enums.ReportTargetType;
import com.whu.treehole.domain.enums.AccountStatus;
import com.whu.treehole.infra.mapper.AuthMapper;
import com.whu.treehole.infra.mapper.PortalCommandMapper;
import com.whu.treehole.infra.mapper.PortalQueryMapper;
import com.whu.treehole.infra.model.PermissionData;
import com.whu.treehole.infra.model.PostData;
import com.whu.treehole.infra.model.ReportData;
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

    @Mock
    private PortalCommandMapper portalCommandMapper;

    @Mock
    private PortalQueryMapper portalQueryMapper;

    private ModerationService moderationService;

    @BeforeEach
    void setUp() {
        moderationService = new ModerationService(
                authMapper,
                portalCommandMapper,
                portalQueryMapper,
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

    @Test
    void shouldCreateReportWhenUserHasPermission() {
        PermissionData reportCreate = new PermissionData();
        reportCreate.setCode("report.create");

        when(authMapper.selectAccountStatusByUserId(9L)).thenReturn(AccountStatus.ACTIVE.name());
        when(authMapper.selectPermissionsByUserId(9L)).thenReturn(List.of(reportCreate));
        doAnswer(invocation -> {
            ReportData reportData = invocation.getArgument(0);
            reportData.setId(33L);
            return null;
        }).when(portalCommandMapper).insertReport(any(ReportData.class));

        ReportSummaryDto summary = moderationService.createReport(
                9L,
                new ReportCreateRequest(ReportTargetType.POST, "home-1", "SPAM", "重复广告"));

        verify(portalCommandMapper).insertReport(any(ReportData.class));
        verify(auditLogService).record("CREATE_REPORT", 9L, "REPORT", 33L, summary.reportCode());
        verify(auditLogService).record("CREATE_REPORT_TARGET", 9L, "POST", null, "home-1");
        org.junit.jupiter.api.Assertions.assertEquals(ReportStatus.OPEN, summary.status());
    }

    @Test
    void shouldAllowAdminToRestoreDeletedPost() {
        PermissionData restorePost = new PermissionData();
        restorePost.setCode("post.restore.any");

        PostData postData = new PostData();
        postData.setId(8L);
        postData.setPostCode("home-1");
        postData.setDeletedFlag(true);

        when(authMapper.selectAccountStatusByUserId(7L)).thenReturn(AccountStatus.ACTIVE.name());
        when(authMapper.selectPermissionsByUserId(7L)).thenReturn(List.of(restorePost));
        when(portalQueryMapper.selectPostByCodeIncludingDeleted("home-1")).thenReturn(postData);

        moderationService.restorePost(7L, "home-1");

        verify(portalCommandMapper).restorePost(eq(8L), any());
        verify(auditLogService).record("RESTORE_POST", 7L, "POST", 8L, "home-1");
    }

    @Test
    void shouldAllowAdminToUnbanNormalUser() {
        PermissionData unbanUser = new PermissionData();
        unbanUser.setCode("user.unban");

        RoleData adminRole = new RoleData();
        adminRole.setId(2L);
        adminRole.setCode("ADMIN");

        RoleData userRole = new RoleData();
        userRole.setId(3L);
        userRole.setCode("USER");

        when(authMapper.selectAccountStatusByUserId(7L)).thenReturn(AccountStatus.ACTIVE.name());
        when(authMapper.selectPermissionsByUserId(7L)).thenReturn(List.of(unbanUser));
        when(authMapper.selectRolesByUserId(7L)).thenReturn(List.of(adminRole));
        when(authMapper.selectUserIdByUserCode("user-12")).thenReturn(12L);
        when(authMapper.selectRolesByUserId(12L)).thenReturn(List.of(userRole));

        moderationService.unbanUser(7L, "user-12");

        verify(authMapper).updateAccountStatus(eq(12L), eq("ACTIVE"), eq(""), any(), eq(7L));
        verify(auditLogService).record("UNBAN_USER", 7L, "USER", 12L, "user-12");
    }

    @Test
    void shouldResolveReportWhenModeratorHasPermission() {
        PermissionData resolveReport = new PermissionData();
        resolveReport.setCode("report.resolve");

        ReportData reportData = new ReportData();
        reportData.setId(41L);
        reportData.setReportCode("report-1");
        reportData.setStatus("OPEN");

        when(authMapper.selectAccountStatusByUserId(7L)).thenReturn(AccountStatus.ACTIVE.name());
        when(authMapper.selectPermissionsByUserId(7L)).thenReturn(List.of(resolveReport));
        when(portalQueryMapper.selectReportByCode("report-1")).thenReturn(reportData);

        moderationService.resolveReport(7L, "report-1", new ReportResolveRequest("DELETE_POST", "spam"));

        verify(portalCommandMapper).resolveReport(eq("report-1"), eq("RESOLVED"), eq("DELETE_POST"), eq("spam"), any(), eq(7L));
        verify(auditLogService).record("RESOLVE_REPORT", 7L, "REPORT", 41L, "report-1");
    }
}
