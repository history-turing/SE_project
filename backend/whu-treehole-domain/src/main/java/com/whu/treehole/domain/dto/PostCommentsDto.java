package com.whu.treehole.domain.dto;

import java.util.List;

public record PostCommentsDto(List<PostCommentDto> comments, Integer total) {
}
