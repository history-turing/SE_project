package com.whu.treehole.infra.model;

/* 帖子互动状态用于点赞和收藏切换。 */

import lombok.Data;

@Data
public class InteractionStateData {
    private Long postId;
    private Long userId;
    private Boolean liked;
    private Boolean saved;
}
