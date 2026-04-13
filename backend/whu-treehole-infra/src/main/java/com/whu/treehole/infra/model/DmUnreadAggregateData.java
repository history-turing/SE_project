package com.whu.treehole.infra.model;

import lombok.Data;

@Data
public class DmUnreadAggregateData {
    private Long userId;
    private Integer messagesUnread;
}
