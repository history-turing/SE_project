package com.whu.treehole.infra.model;

/* 关注状态对象用于校友联系人关注切换。 */

import lombok.Data;

@Data
public class FollowStateData {
    private Long contactId;
    private Long userId;
    private Boolean followed;
}
