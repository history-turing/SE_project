package com.whu.treehole.infra.model;

/* 公告数据用于首页通知区。 */

import lombok.Data;

@Data
public class NoticeData {
    private String noticeCode;
    private String title;
    private String meta;
    private Integer sortOrder;
}
