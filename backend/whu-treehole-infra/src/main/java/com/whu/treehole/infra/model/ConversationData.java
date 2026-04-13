package com.whu.treehole.infra.model;

/* 会话摘要数据用于消息列表。 */

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ConversationData {
    private Long id;
    private String conversationCode;
    private String conversationType;
    private String peerName;
    private String peerSubtitle;
    private String peerAvatarUrl;
    private String lastMessage;
    private String displayTime;
    private Integer unreadCount;
    private LocalDateTime sortTime;
}
