package com.whu.treehole.domain.dto;

/* 帖子卡片直接对应前端通用 PostCard 组件。 */

public record PostCardDto(
        String id,
        String title,
        String content,
        String author,
        String authorUserCode,
        String handle,
        String topic,
        String audience,
        String createdAt,
        Integer likes,
        Integer comments,
        Integer saves,
        String accent,
        String badge,
        String image,
        Boolean anonymous,
        String location,
        Boolean mine,
        Boolean canDelete,
        Boolean liked,
        Boolean saved
) {
}
