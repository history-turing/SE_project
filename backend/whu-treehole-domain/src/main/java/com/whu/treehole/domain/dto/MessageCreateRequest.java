package com.whu.treehole.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/* 发送消息请求用于校验文本内容。 */

public record MessageCreateRequest(
        @NotBlank(message = "消息内容不能为空")
        @Size(max = 1000, message = "消息长度不能超过1000个字符")
        String text
) {
}
