package com.whu.treehole.infra.model;

import com.whu.treehole.domain.enums.ConversationStatus;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DmConversationData {
    private Long id;
    private String conversationCode;
    private String conversationType;
    private String conversationScene;
    private String sourcePostCode;
    private Boolean anonymousFlag;
    private ConversationStatus status;
    private Long createdBy;
    private Long lastMessageId;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
