package com.whu.treehole.infra.model;

/* 话题基础数据用于广场页与首页入口展示。 */

import lombok.Data;

@Data
public class TopicData {
    private Long id;
    private String topicCode;
    private String name;
    private String description;
    private String heatText;
    private String destinationType;
    private String accentTone;
    private String emoji;
    private Integer sortOrder;
}
