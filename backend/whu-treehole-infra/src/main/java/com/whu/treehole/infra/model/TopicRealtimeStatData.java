package com.whu.treehole.infra.model;

import lombok.Data;

@Data
public class TopicRealtimeStatData {
    private String topicCode;
    private String topicName;
    private Integer todayPostCount;
    private Integer interactionCount24h;
    private Integer totalPostCount;
}
