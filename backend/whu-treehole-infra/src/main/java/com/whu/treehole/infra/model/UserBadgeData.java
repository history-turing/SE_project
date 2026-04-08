package com.whu.treehole.infra.model;

/* 勋章数据用于个人页标签展示。 */

import lombok.Data;

@Data
public class UserBadgeData {
    private String badgeName;
    private Integer sortOrder;
}
