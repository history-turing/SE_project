package com.whu.treehole.infra.model;

/* 话题标签与话题主体拆表，便于一对多维护。 */

import lombok.Data;

@Data
public class TopicTagData {
    private String topicCode;
    private String tagName;
    private Integer sortOrder;
}
