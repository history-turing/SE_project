package com.whu.treehole.infra.model;

/* 热榜项用于首页和话题页共享。 */

import lombok.Data;

@Data
public class RankingData {
    private String rankingCode;
    private String label;
    private String heatText;
    private Integer sortOrder;
}
