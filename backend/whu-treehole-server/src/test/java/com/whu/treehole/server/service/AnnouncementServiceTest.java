package com.whu.treehole.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.dto.AnnouncementSaveRequest;
import com.whu.treehole.domain.dto.AnnouncementSummaryDto;
import com.whu.treehole.infra.mapper.AuthMapper;
import com.whu.treehole.infra.mapper.PortalCommandMapper;
import com.whu.treehole.infra.mapper.PortalQueryMapper;
import com.whu.treehole.infra.model.AnnouncementData;
import com.whu.treehole.infra.model.PermissionData;
import com.whu.treehole.domain.enums.AccountStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-04-12T04:00:00Z"),
            ZoneId.of("Asia/Shanghai"));

    @Mock
    private AuthMapper authMapper;

    @Mock
    private PortalQueryMapper portalQueryMapper;

    @Mock
    private PortalCommandMapper portalCommandMapper;

    @Mock
    private AuditLogService auditLogService;

    private AnnouncementService announcementService;

    @BeforeEach
    void setUp() {
        announcementService = new AnnouncementService(
                portalQueryMapper,
                portalCommandMapper,
                new AuthorizationService(authMapper),
                auditLogService,
                FIXED_CLOCK);
    }

    @Test
    void shouldReturnHomeAnnouncementsAndPopupAnnouncement() {
        when(portalQueryMapper.selectActiveAnnouncements(any())).thenReturn(List.of(
                announcement("announcement-1", "维护通知", "今晚维护", "SYSTEM", true, false, LocalDateTime.of(2026, 4, 12, 8, 0)),
                announcement("announcement-2", "社团活动", "周末活动报名", "ACTIVITY", false, false, LocalDateTime.of(2026, 4, 12, 9, 0)),
                announcement("announcement-3", "校庆公告", "校庆安排", "CAMPUS", false, false, LocalDateTime.of(2026, 4, 12, 10, 0)),
                announcement("announcement-4", "弹窗公告", "登录后弹出", "SYSTEM", true, true, LocalDateTime.of(2026, 4, 12, 11, 0))
        ));
        when(portalQueryMapper.selectActivePopupAnnouncement(any())).thenReturn(
                announcement("announcement-4", "弹窗公告", "登录后弹出", "SYSTEM", true, true, LocalDateTime.of(2026, 4, 12, 11, 0))
        );

        List<AnnouncementSummaryDto> homeAnnouncements = announcementService.listHomeAnnouncements();

        assertEquals(3, homeAnnouncements.size());
        assertEquals("announcement-1", homeAnnouncements.get(0).code());
        assertEquals("announcement-4", announcementService.getActivePopupAnnouncement().code());
    }

    @Test
    void shouldRequirePopupPermissionWhenCreatingPopupAnnouncement() {
        PermissionData createPermission = new PermissionData();
        createPermission.setCode("announcement.create");
        PermissionData publishPermission = new PermissionData();
        publishPermission.setCode("announcement.publish");

        when(authMapper.selectAccountStatusByUserId(7L)).thenReturn(AccountStatus.ACTIVE.name());
        when(authMapper.selectPermissionsByUserId(7L)).thenReturn(List.of(createPermission, publishPermission));

        AnnouncementSaveRequest request = new AnnouncementSaveRequest(
                "系统提醒",
                "请及时关注",
                "完整内容",
                "SYSTEM",
                true,
                true,
                true,
                LocalDateTime.of(2026, 4, 12, 12, 0),
                null
        );

        assertThrows(BusinessException.class, () -> announcementService.createAnnouncement(7L, request));
    }

    @Test
    void shouldAllowSuperAdminToCreatePopupAnnouncement() {
        PermissionData createPermission = new PermissionData();
        createPermission.setCode("announcement.create");
        PermissionData publishPermission = new PermissionData();
        publishPermission.setCode("announcement.publish");
        PermissionData popupPermission = new PermissionData();
        popupPermission.setCode("announcement.popup.manage");

        when(authMapper.selectAccountStatusByUserId(9L)).thenReturn(AccountStatus.ACTIVE.name());
        when(authMapper.selectPermissionsByUserId(9L)).thenReturn(List.of(createPermission, publishPermission, popupPermission));
        doAnswer(invocation -> {
            AnnouncementData data = invocation.getArgument(0);
            data.setId(88L);
            data.setAnnouncementCode("announcement-88");
            return null;
        }).when(portalCommandMapper).insertAnnouncement(any(AnnouncementData.class));

        AnnouncementSaveRequest request = new AnnouncementSaveRequest(
                "系统提醒",
                "请及时关注",
                "完整内容",
                "SYSTEM",
                true,
                true,
                true,
                LocalDateTime.of(2026, 4, 12, 12, 0),
                null
        );

        announcementService.createAnnouncement(9L, request);

        verify(portalCommandMapper).insertAnnouncement(any(AnnouncementData.class));
        verify(auditLogService).record("CREATE_ANNOUNCEMENT", 9L, "ANNOUNCEMENT", 88L, "announcement-88");
    }

    private static AnnouncementData announcement(String code,
                                                 String title,
                                                 String summary,
                                                 String category,
                                                 boolean pinned,
                                                 boolean popup,
                                                 LocalDateTime publishedAt) {
        AnnouncementData data = new AnnouncementData();
        data.setAnnouncementCode(code);
        data.setTitle(title);
        data.setSummary(summary);
        data.setContent(summary);
        data.setCategory(category);
        data.setPinnedFlag(pinned);
        data.setPopupFlag(popup);
        data.setPopupOncePerSession(true);
        data.setStatus("PUBLISHED");
        data.setPublishedAt(publishedAt);
        return data;
    }
}
