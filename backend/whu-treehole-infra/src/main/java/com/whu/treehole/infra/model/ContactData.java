package com.whu.treehole.infra.model;

/* 校友联系人数据同时包含当前用户的关注状态。 */

import lombok.Data;

@Data
public class ContactData {
    private Long id;
    private String contactCode;
    private String name;
    private String meta;
    private String focus;
    private String avatarUrl;
    private Boolean followed;
    private Integer sortOrder;
}
