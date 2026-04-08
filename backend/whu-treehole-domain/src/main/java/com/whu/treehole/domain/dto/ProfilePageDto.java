package com.whu.treehole.domain.dto;

import java.util.List;

/* 个人主页接口聚合资料、帖子、收藏与消息会话。 */

public record ProfilePageDto(
        UserProfileDto profile,
        List<PostCardDto> myPosts,
        List<PostCardDto> savedPosts,
        List<ConversationDto> conversations,
        String activeConversationId
) {
}
