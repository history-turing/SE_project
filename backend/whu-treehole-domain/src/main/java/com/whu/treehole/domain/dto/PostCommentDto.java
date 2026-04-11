package com.whu.treehole.domain.dto;

import java.util.List;

public record PostCommentDto(
        String id,
        String postId,
        String parentCommentCode,
        String author,
        String handle,
        String content,
        String createdAt,
        Boolean mine,
        Boolean canDelete,
        String replyToUserName,
        List<PostCommentDto> replies
) {
}
