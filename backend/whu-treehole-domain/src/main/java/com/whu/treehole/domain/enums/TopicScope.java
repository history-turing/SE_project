package com.whu.treehole.domain.enums;

/* 话题广场按页面范围划分，供查询接口统一解析。 */

public enum TopicScope {
    ALL,
    CAMPUS,
    ALUMNI;

    public static TopicScope from(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        return TopicScope.valueOf(value.toUpperCase());
    }
}
