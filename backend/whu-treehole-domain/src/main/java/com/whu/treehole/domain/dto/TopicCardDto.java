package com.whu.treehole.domain.dto;

import java.util.List;

/* 话题卡片承载广场页和首页入口的展示字段。 */

public record TopicCardDto(
        String id,
        String name,
        String description,
        String heat,
        String destination,
        String accent,
        List<String> tags,
        String emoji
) {
}
