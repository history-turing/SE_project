package com.whu.treehole.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record AnnouncementSaveRequest(
        @NotBlank(message = "公告标题不能为空")
        String title,
        @NotBlank(message = "公告摘要不能为空")
        String summary,
        @NotBlank(message = "公告内容不能为空")
        String content,
        @NotBlank(message = "公告分类不能为空")
        String category,
        @NotNull(message = "是否置顶不能为空")
        Boolean pinned,
        @NotNull(message = "是否弹窗不能为空")
        Boolean popupEnabled,
        @NotNull(message = "弹窗会话策略不能为空")
        Boolean popupOncePerSession,
        LocalDateTime publishedAt,
        LocalDateTime expireAt
) {
}
