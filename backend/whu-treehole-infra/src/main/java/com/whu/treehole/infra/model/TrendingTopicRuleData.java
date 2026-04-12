package com.whu.treehole.infra.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TrendingTopicRuleData {
    private Long id;
    private String topicKey;
    private String displayName;
    private String mergeTargetKey;
    private Boolean hiddenFlag;
    private Boolean pinnedFlag;
    private Integer sortOrder;
    private Long updatedBy;
    private LocalDateTime updatedAt;
}
