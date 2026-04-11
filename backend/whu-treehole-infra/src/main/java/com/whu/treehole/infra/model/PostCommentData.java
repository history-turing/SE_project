package com.whu.treehole.infra.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PostCommentData {
    private Long id;
    private String commentCode;
    private Long postId;
    private Long userId;
    private Long parentCommentId;
    private Long rootCommentId;
    private Long replyToUserId;
    private String authorName;
    private String authorHandle;
    private String content;
    private Boolean deletedFlag;
    private LocalDateTime deletedAt;
    private Long deletedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
