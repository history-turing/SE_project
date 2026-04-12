package com.whu.treehole.infra.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DmConversationEventData {
    private Long id;
    private Long conversationId;
    private String eventType;
    private Long operatorUserId;
    private String payload;
    private LocalDateTime createdAt;
}
