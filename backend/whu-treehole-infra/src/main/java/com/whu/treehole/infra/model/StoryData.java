package com.whu.treehole.infra.model;

/* 校友故事卡片用于校友圈侧栏。 */

import lombok.Data;

@Data
public class StoryData {
    private String storyCode;
    private String title;
    private String meta;
    private Integer sortOrder;
}
