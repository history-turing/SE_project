package com.whu.treehole.domain.dto;

/* 当前登录用户摘要信息。 */

public record AuthUserDto(
        Long id,
        String username,
        String email,
        String name,
        String avatar
) {
}
