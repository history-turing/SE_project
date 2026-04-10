package com.whu.treehole.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @NotBlank(message = "评论内容不能为空")
        @Size(max = 1000, message = "评论内容不能超过1000个字符")
        String content
) {
}
