package com.whu.treehole.infra.model;

/* 消息数据对象用于会话明细查询和写入。 */

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MessageData {
    private Long id;
    private String messageCode;
    private Long conversationId;
    private String conversationCode;
    private String senderType;
    private String textContent;
    private String displayTime;
    private LocalDateTime createdAt;
}
