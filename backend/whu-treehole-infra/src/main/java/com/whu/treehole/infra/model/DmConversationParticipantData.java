package com.whu.treehole.infra.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DmConversationParticipantData {
    private Long id;
    private Long conversationId;
    private Long userId;
    private Long lastReadMessageId;
    private LocalDateTime lastReadAt;
    private Integer unreadCount;
    private Boolean pinnedFlag;
    private Boolean mutedFlag;
    private LocalDateTime clearedAt;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
