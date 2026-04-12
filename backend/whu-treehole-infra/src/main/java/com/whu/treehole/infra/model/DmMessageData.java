package com.whu.treehole.infra.model;

import com.whu.treehole.domain.enums.MessageStatus;
import com.whu.treehole.domain.enums.MessageType;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DmMessageData {
    private Long id;
    private String messageCode;
    private String clientMessageId;
    private Long conversationId;
    private Long senderUserId;
    private MessageType messageType;
    private MessageStatus status;
    private String contentPayload;
    private LocalDateTime sentAt;
    private LocalDateTime recalledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
