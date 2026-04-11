package com.whu.treehole.server.service;

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.dto.AuditLogDto;
import com.whu.treehole.domain.dto.AdminUserDto;
import com.whu.treehole.domain.dto.ReportCreateRequest;
import com.whu.treehole.domain.dto.ReportResolveRequest;
import com.whu.treehole.domain.dto.ReportSummaryDto;
import com.whu.treehole.domain.dto.RoleDto;
import com.whu.treehole.domain.enums.AccountStatus;
import com.whu.treehole.domain.enums.ReportResolutionCode;
import com.whu.treehole.domain.enums.ReportStatus;
import com.whu.treehole.domain.enums.ReportTargetType;
import com.whu.treehole.infra.mapper.AuthMapper;
import com.whu.treehole.infra.mapper.PortalCommandMapper;
import com.whu.treehole.infra.mapper.PortalQueryMapper;
import com.whu.treehole.infra.model.PostCommentData;
import com.whu.treehole.infra.model.PostData;
import com.whu.treehole.infra.model.ReportData;
import com.whu.treehole.infra.model.RoleData;
import com.whu.treehole.infra.model.UserRoleData;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class ModerationService {

    private final AuthMapper authMapper;
    private final PortalCommandMapper portalCommandMapper;
    private final PortalQueryMapper portalQueryMapper;
    private final AuthorizationService authorizationService;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public ModerationService(AuthMapper authMapper,
                             PortalCommandMapper portalCommandMapper,
                             PortalQueryMapper portalQueryMapper,
                             AuthorizationService authorizationService,
                             AuditLogService auditLogService,
                             Clock clock) {
        this.authMapper = authMapper;
        this.portalCommandMapper = portalCommandMapper;
        this.portalQueryMapper = portalQueryMapper;
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

    public void unbanUser(long actorUserId, String userCode) {
        authorizationService.assertCanWrite(actorUserId, "user.unban");

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
                AccountStatus.ACTIVE.name(),
                "",
                LocalDateTime.now(clock),
                actorUserId);
        auditLogService.record("UNBAN_USER", actorUserId, "USER", targetUserId, userCode);
    }

    public ReportSummaryDto createReport(long actorUserId, ReportCreateRequest request) {
        authorizationService.assertCanWrite(actorUserId, "report.create");

        LocalDateTime now = LocalDateTime.now(clock);
        ReportData reportData = new ReportData();
        reportData.setReportCode("report-" + System.currentTimeMillis());
        reportData.setTargetType(request.targetType().name());
        reportData.setTargetCode(request.targetCode().trim());
        reportData.setReporterUserId(actorUserId);
        reportData.setReasonCode(request.reasonCode().trim());
        reportData.setReasonDetail(blankToNull(request.reasonDetail()));
        reportData.setStatus(ReportStatus.OPEN.name());
        reportData.setCreatedAt(now);
        reportData.setUpdatedAt(now);
        portalCommandMapper.insertReport(reportData);

        auditLogService.record("CREATE_REPORT", actorUserId, "REPORT", reportData.getId(), reportData.getReportCode());
        auditLogService.record("CREATE_REPORT_TARGET", actorUserId, request.targetType().name(), null, request.targetCode().trim());

        return toReportSummary(reportData);
    }

    public void restorePost(long actorUserId, String postCode) {
        authorizationService.assertCanWrite(actorUserId, "post.restore.any");

        PostData postData = portalQueryMapper.selectPostByCodeIncludingDeleted(postCode);
        if (postData == null) {
            throw new BusinessException(4041, "帖子不存在");
        }
        portalCommandMapper.restorePost(postData.getId(), LocalDateTime.now(clock));
        auditLogService.record("RESTORE_POST", actorUserId, "POST", postData.getId(), postCode);
    }

    public void restoreComment(long actorUserId, String postCode, String commentCode) {
        authorizationService.assertCanWrite(actorUserId, "comment.restore.any");

        PostCommentData commentData = portalQueryMapper.selectCommentByCodeIncludingDeleted(postCode, commentCode);
        if (commentData == null) {
            throw new BusinessException(4044, "评论不存在");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        if (commentData.getParentCommentId() == null) {
            portalCommandMapper.restoreCommentBranch(commentData.getId(), now);
        } else {
            portalCommandMapper.restoreComment(commentData.getId(), now);
        }
        auditLogService.record("RESTORE_COMMENT", actorUserId, "COMMENT", commentData.getId(), commentCode);
    }

    public List<ReportSummaryDto> listReports(long actorUserId) {
        authorizationService.assertCanWrite(actorUserId, "report.read.any");
        return portalQueryMapper.selectReports().stream()
                .map(this::toReportSummary)
                .toList();
    }

    public void resolveReport(long actorUserId, String reportCode, ReportResolveRequest request) {
        authorizationService.assertCanWrite(actorUserId, "report.resolve");

        ReportData reportData = portalQueryMapper.selectReportByCode(reportCode);
        if (reportData == null) {
            throw new BusinessException(4046, "REPORT_NOT_FOUND");
        }

        String resolutionCode = normalizeResolutionCode(request.resolutionCode());
        portalCommandMapper.resolveReport(
                reportCode,
                ReportStatus.RESOLVED.name(),
                resolutionCode,
                blankToNull(request.resolutionNote()),
                LocalDateTime.now(clock),
                actorUserId);
        auditLogService.record("RESOLVE_REPORT", actorUserId, "REPORT", reportData.getId(), reportCode);
    }

    public List<AuditLogDto> listAuditLogs(long actorUserId) {
        boolean canReadAll = authorizationService.hasPermission(actorUserId, "audit.read.all");
        boolean canReadModeration = authorizationService.hasPermission(actorUserId, "audit.read.moderation");
        if (!canReadAll && !canReadModeration) {
            throw new BusinessException(4030, "FORBIDDEN");
        }
        return portalQueryMapper.selectAuditLogs().stream()
                .map(log -> new AuditLogDto(
                        log.getId(),
                        log.getActorUserId(),
                        log.getActorRoleSnapshot(),
                        log.getActionType(),
                        log.getTargetType(),
                        log.getTargetId(),
                        log.getTargetCode(),
                        log.getCreatedAt() == null ? "" : log.getCreatedAt().toString()))
                .toList();
    }

    public List<RoleDto> listRoles(long actorUserId) {
        authorizationService.assertCanWrite(actorUserId, "role.read.any");
        return authMapper.selectAllRoles().stream()
                .map(role -> new RoleDto(role.getId(), role.getCode(), role.getName()))
                .toList();
    }

    public List<AdminUserDto> listUsers(long actorUserId) {
        boolean canReadRoles = authorizationService.hasPermission(actorUserId, "role.read.any");
        boolean canModerateUsers = authorizationService.hasPermission(actorUserId, "user.ban");
        if (!canReadRoles && !canModerateUsers) {
            throw new BusinessException(4030, "FORBIDDEN");
        }

        Map<Long, AdminUserDtoBuilder> users = new LinkedHashMap<>();
        for (UserRoleData row : authMapper.selectAdminUsers()) {
            AdminUserDtoBuilder builder = users.computeIfAbsent(row.getUserId(), ignored -> new AdminUserDtoBuilder(
                    row.getUserId(),
                    row.getUserCode(),
                    row.getUsername(),
                    row.getName(),
                    parseAccountStatus(row.getAccountStatus())));
            if (row.getRoleId() != null) {
                builder.roles().add(new RoleDto(row.getRoleId(), row.getRoleCode(), row.getRoleName()));
            }
        }
        return users.values().stream().map(AdminUserDtoBuilder::build).toList();
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

    private ReportSummaryDto toReportSummary(ReportData reportData) {
        return new ReportSummaryDto(
                reportData.getReportCode(),
                ReportTargetType.valueOf(reportData.getTargetType()),
                reportData.getTargetCode(),
                reportData.getReasonCode(),
                reportData.getReasonDetail(),
                ReportStatus.valueOf(reportData.getStatus()),
                reportData.getResolutionCode(),
                reportData.getResolutionNote(),
                reportData.getCreatedAt() == null ? "" : reportData.getCreatedAt().toString());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeResolutionCode(String resolutionCode) {
        try {
            return ReportResolutionCode.valueOf(resolutionCode.trim().toUpperCase(Locale.ROOT)).name();
        } catch (RuntimeException exception) {
            throw new BusinessException(4006, "INVALID_REPORT_RESOLUTION");
        }
    }

    private AccountStatus parseAccountStatus(String code) {
        if (code == null || code.isBlank()) {
            return AccountStatus.ACTIVE;
        }
        try {
            return AccountStatus.valueOf(code.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return AccountStatus.ACTIVE;
        }
    }

    private record AdminUserDtoBuilder(
            Long id,
            String userCode,
            String username,
            String name,
            AccountStatus accountStatus,
            List<RoleDto> roles
    ) {
        private AdminUserDtoBuilder(Long id,
                                    String userCode,
                                    String username,
                                    String name,
                                    AccountStatus accountStatus) {
            this(id, userCode, username, name, accountStatus, new java.util.ArrayList<>());
        }

        private AdminUserDto build() {
            return new AdminUserDto(id, userCode, username, name, accountStatus, List.copyOf(roles));
        }
    }
}
