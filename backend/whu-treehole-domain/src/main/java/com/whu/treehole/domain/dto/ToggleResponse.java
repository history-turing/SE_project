package com.whu.treehole.domain.dto;

/* 通用切换响应可用于点赞、收藏和关注等状态回写。 */

public record ToggleResponse(String targetId, Boolean active, Integer count) {
}
