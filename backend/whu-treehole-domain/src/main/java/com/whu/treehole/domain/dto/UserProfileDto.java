package com.whu.treehole.domain.dto;

import java.util.List;

/* 个人主页基础资料包含标签和统计信息。 */

public record UserProfileDto(
        String name,
        String tagline,
        String college,
        String year,
        String bio,
        String avatar,
        List<String> badges,
        List<ProfileStatDto> stats
) {
}
