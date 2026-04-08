package com.whu.treehole.domain.dto;

import java.util.List;

/* 首页接口返回页面所需的完整聚合数据。 */

public record HomePageDto(
        HomeStatsDto stats,
        List<TopicCardDto> topicHighlights,
        List<RankingItemDto> rankings,
        List<NoticeItemDto> notices,
        List<PostCardDto> posts
) {
}
