package com.whu.treehole.domain.enums;

/* 发布范围统一使用枚举管理，兼容前端标签与后端代码值。 */

public enum AudienceType {
    HOME("HOME", "首页", "/"),
    ALUMNI("ALUMNI", "校友圈", "/alumni");

    private final String code;
    private final String label;
    private final String destination;

    AudienceType(String code, String label, String destination) {
        this.code = code;
        this.label = label;
        this.destination = destination;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public String destination() {
        return destination;
    }

    public static AudienceType fromLabel(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("发布范围不能为空");
        }

        for (AudienceType type : values()) {
            if (type.label.equals(value) || type.code.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("不支持的发布范围: " + value);
    }
}
