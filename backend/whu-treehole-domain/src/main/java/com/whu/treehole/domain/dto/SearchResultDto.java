package com.whu.treehole.domain.dto;

import java.util.List;

public record SearchResultDto(
        String keyword,
        Integer total,
        List<PostCardDto> posts,
        List<StoryCardDto> stories,
        List<AlumniContactDto> contacts
) {
}
