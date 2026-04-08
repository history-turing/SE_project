package com.whu.treehole.domain.dto;

import java.util.List;

/* 话题广场接口返回话题列表和排行信息。 */

public record TopicsPageDto(List<TopicCardDto> topics, List<RankingItemDto> rankings) {
}
