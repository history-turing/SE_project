package com.whu.treehole.infra.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AnnouncementData {
    private Long id;
    private String announcementCode;
    private String title;
    private String summary;
    private String content;
    private String category;
    private String status;
    private Boolean pinnedFlag;
    private Boolean popupFlag;
    private Boolean popupOncePerSession;
    private LocalDateTime publishedAt;
    private LocalDateTime expireAt;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
