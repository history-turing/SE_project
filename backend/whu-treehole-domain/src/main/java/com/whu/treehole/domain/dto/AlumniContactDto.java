package com.whu.treehole.domain.dto;

/* 联系人卡片用于校友圈关注区展示。 */

public record AlumniContactDto(
        String id,
        String name,
        String meta,
        String focus,
        String avatar,
        Boolean followed
) {
}
