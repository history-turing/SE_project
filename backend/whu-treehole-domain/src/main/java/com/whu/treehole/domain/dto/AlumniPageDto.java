package com.whu.treehole.domain.dto;

import java.util.List;

/* 校友圈接口聚合故事、人脉联系人与帖子。 */

public record AlumniPageDto(
        List<StoryCardDto> stories,
        List<AlumniContactDto> contacts,
        List<PostCardDto> posts
) {
}
