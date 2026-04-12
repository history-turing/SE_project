package com.whu.treehole.infra.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DmUserBlockData {
    private Long id;
    private Long userId;
    private Long blockedUserId;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
