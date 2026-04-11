package com.whu.treehole.infra.model;

/* 帖子数据对象承接数据库字段并供应用层组装页面卡片。 */

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PostData {
    private Long id;
    private String postCode;
    private Long creatorUserId;
    private String title;
    private String content;
    private String authorName;
    private String authorHandle;
    private String topicName;
    private String audienceType;
    private String displayTime;
    private Integer likeCount;
    private Integer commentCount;
    private Integer saveCount;
    private String accentTone;
    private String badge;
    private String imageUrl;
    private Boolean anonymousFlag;
    private String location;
    private Boolean deletedFlag;
    private LocalDateTime deletedAt;
    private Long deletedBy;
    private LocalDateTime createdAt;
    private Boolean liked;
    private Boolean saved;
}
