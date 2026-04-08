package com.whu.treehole.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/* 发帖请求与前端发布弹窗字段保持一致。 */

public record PostCreateRequest(
        @Size(max = 255, message = "标题长度不能超过255个字符")
        String title,
        @NotBlank(message = "正文不能为空")
        @Size(max = 4000, message = "正文长度不能超过4000个字符")
        String content,
        @NotBlank(message = "话题不能为空")
        @Size(max = 64, message = "话题长度不能超过64个字符")
        String topic,
        @NotBlank(message = "发布范围不能为空")
        String audience,
        @NotNull(message = "匿名标识不能为空")
        Boolean anonymous
) {
}
