package com.whu.treehole.server.service;

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.dto.AnnouncementDetailDto;
import com.whu.treehole.domain.dto.AnnouncementPopupDto;
import com.whu.treehole.domain.dto.AnnouncementSaveRequest;
import com.whu.treehole.domain.dto.AnnouncementSummaryDto;
import com.whu.treehole.infra.mapper.PortalCommandMapper;
import com.whu.treehole.infra.mapper.PortalQueryMapper;
import com.whu.treehole.infra.model.AnnouncementData;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnnouncementService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PortalQueryMapper portalQueryMapper;
    private final PortalCommandMapper portalCommandMapper;
    private final AuthorizationService authorizationService;
    private final AuditLogService auditLogService;
    private final Clock clock;

    @Autowired
    public AnnouncementService(PortalQueryMapper portalQueryMapper,
                               PortalCommandMapper portalCommandMapper,
                               AuthorizationService authorizationService,
                               AuditLogService auditLogService,
                               Clock clock) {
        this.portalQueryMapper = portalQueryMapper;
        this.portalCommandMapper = portalCommandMapper;
        this.authorizationService = authorizationService;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    public List<AnnouncementSummaryDto> listHomeAnnouncements() {
        return portalQueryMapper.selectActiveAnnouncements(LocalDateTime.now(clock)).stream()
                .limit(3)
                .map(this::toSummary)
                .toList();
    }

    public List<AnnouncementSummaryDto> listPublishedAnnouncements() {
        return portalQueryMapper.selectActiveAnnouncements(LocalDateTime.now(clock)).stream()
                .map(this::toSummary)
                .toList();
    }

    public AnnouncementPopupDto getActivePopupAnnouncement() {
        AnnouncementData data = portalQueryMapper.selectActivePopupAnnouncement(LocalDateTime.now(clock));
        if (data == null) {
            return null;
        }
        return new AnnouncementPopupDto(
                data.getAnnouncementCode(),
                data.getTitle(),
                data.getContent(),
                Boolean.TRUE.equals(data.getPopupOncePerSession())
        );
    }

    public AnnouncementDetailDto getAnnouncementDetail(String announcementCode) {
        AnnouncementData data = portalQueryMapper.selectAnnouncementByCode(announcementCode);
        if (data == null) {
            throw new BusinessException(4047, "ANNOUNCEMENT_NOT_FOUND");
        }
        return toDetail(data);
    }

    public List<AnnouncementSummaryDto> listAdminAnnouncements(long actorUserId) {
        authorizationService.assertCanWrite(actorUserId, "announcement.read.any");
        return portalQueryMapper.selectAdminAnnouncements().stream()
                .map(this::toSummary)
                .toList();
    }

    public AnnouncementSummaryDto createAnnouncement(long actorUserId, AnnouncementSaveRequest request) {
        authorizationService.assertCanWrite(actorUserId, "announcement.create");
        assertCanManageAdvancedFlags(actorUserId, request);

        LocalDateTime now = LocalDateTime.now(clock);
        AnnouncementData data = new AnnouncementData();
        data.setAnnouncementCode("announcement-" + System.currentTimeMillis());
        data.setTitle(request.title().trim());
        data.setSummary(request.summary().trim());
        data.setContent(request.content().trim());
        data.setCategory(request.category().trim());
        data.setStatus("DRAFT");
        data.setPinnedFlag(Boolean.TRUE.equals(request.pinned()));
        data.setPopupFlag(Boolean.TRUE.equals(request.popupEnabled()));
        data.setPopupOncePerSession(Boolean.TRUE.equals(request.popupOncePerSession()));
        data.setPublishedAt(request.publishedAt());
        data.setExpireAt(request.expireAt());
        data.setCreatedBy(actorUserId);
        data.setUpdatedBy(actorUserId);
        data.setCreatedAt(now);
        data.setUpdatedAt(now);
        portalCommandMapper.insertAnnouncement(data);
        auditLogService.record("CREATE_ANNOUNCEMENT", actorUserId, "ANNOUNCEMENT", data.getId(), data.getAnnouncementCode());
        return toSummary(data);
    }

    public AnnouncementSummaryDto updateAnnouncement(long actorUserId, String announcementCode, AnnouncementSaveRequest request) {
        authorizationService.assertCanWrite(actorUserId, "announcement.create");
        assertCanManageAdvancedFlags(actorUserId, request);

        AnnouncementData current = requireAnnouncement(announcementCode);
        current.setTitle(request.title().trim());
        current.setSummary(request.summary().trim());
        current.setContent(request.content().trim());
        current.setCategory(request.category().trim());
        current.setPinnedFlag(Boolean.TRUE.equals(request.pinned()));
        current.setPopupFlag(Boolean.TRUE.equals(request.popupEnabled()));
        current.setPopupOncePerSession(Boolean.TRUE.equals(request.popupOncePerSession()));
        current.setPublishedAt(request.publishedAt());
        current.setExpireAt(request.expireAt());
        current.setUpdatedBy(actorUserId);
        current.setUpdatedAt(LocalDateTime.now(clock));
        portalCommandMapper.updateAnnouncement(current);
        auditLogService.record("UPDATE_ANNOUNCEMENT", actorUserId, "ANNOUNCEMENT", current.getId(), current.getAnnouncementCode());
        return toSummary(current);
    }

    public void publishAnnouncement(long actorUserId, String announcementCode) {
        authorizationService.assertCanWrite(actorUserId, "announcement.publish");
        AnnouncementData current = requireAnnouncement(announcementCode);
        assertCanManageAdvancedFlags(actorUserId, toRequest(current));
        LocalDateTime now = LocalDateTime.now(clock);
        portalCommandMapper.publishAnnouncement(announcementCode, current.getPublishedAt() == null ? now : current.getPublishedAt(), actorUserId, now);
        auditLogService.record("PUBLISH_ANNOUNCEMENT", actorUserId, "ANNOUNCEMENT", current.getId(), announcementCode);
    }

    public void offlineAnnouncement(long actorUserId, String announcementCode) {
        authorizationService.assertCanWrite(actorUserId, "announcement.publish");
        AnnouncementData current = requireAnnouncement(announcementCode);
        LocalDateTime now = LocalDateTime.now(clock);
        portalCommandMapper.offlineAnnouncement(announcementCode, actorUserId, now);
        auditLogService.record("OFFLINE_ANNOUNCEMENT", actorUserId, "ANNOUNCEMENT", current.getId(), announcementCode);
    }

    private void assertCanManageAdvancedFlags(long actorUserId, AnnouncementSaveRequest request) {
        if (Boolean.TRUE.equals(request.pinned()) || Boolean.TRUE.equals(request.popupEnabled())) {
            authorizationService.assertCanWrite(actorUserId, "announcement.popup.manage");
        }
    }

    private AnnouncementData requireAnnouncement(String announcementCode) {
        AnnouncementData data = portalQueryMapper.selectAnnouncementByCode(announcementCode);
        if (data == null) {
            throw new BusinessException(4047, "ANNOUNCEMENT_NOT_FOUND");
        }
        return data;
    }

    private AnnouncementSaveRequest toRequest(AnnouncementData data) {
        return new AnnouncementSaveRequest(
                data.getTitle(),
                data.getSummary(),
                data.getContent(),
                data.getCategory(),
                Boolean.TRUE.equals(data.getPinnedFlag()),
                Boolean.TRUE.equals(data.getPopupFlag()),
                Boolean.TRUE.equals(data.getPopupOncePerSession()),
                data.getPublishedAt(),
                data.getExpireAt()
        );
    }

    private AnnouncementSummaryDto toSummary(AnnouncementData data) {
        return new AnnouncementSummaryDto(
                data.getAnnouncementCode(),
                data.getTitle(),
                data.getSummary(),
                data.getCategory(),
                Boolean.TRUE.equals(data.getPinnedFlag()),
                Boolean.TRUE.equals(data.getPopupFlag()),
                Boolean.TRUE.equals(data.getPopupOncePerSession()),
                data.getStatus(),
                format(data.getPublishedAt()),
                format(data.getExpireAt())
        );
    }

    private AnnouncementDetailDto toDetail(AnnouncementData data) {
        return new AnnouncementDetailDto(
                data.getAnnouncementCode(),
                data.getTitle(),
                data.getSummary(),
                data.getContent(),
                data.getCategory(),
                Boolean.TRUE.equals(data.getPinnedFlag()),
                Boolean.TRUE.equals(data.getPopupFlag()),
                Boolean.TRUE.equals(data.getPopupOncePerSession()),
                data.getStatus(),
                format(data.getPublishedAt()),
                format(data.getExpireAt())
        );
    }

    private String format(LocalDateTime value) {
        return value == null ? "" : value.format(FORMATTER);
    }
}
