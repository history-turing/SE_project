package com.whu.treehole.infra.model;

/* 个人页统计卡片采用键值形式存储。 */

import lombok.Data;

@Data
public class ProfileStatData {
    private String statLabel;
    private String statValue;
    private Integer sortOrder;
}
