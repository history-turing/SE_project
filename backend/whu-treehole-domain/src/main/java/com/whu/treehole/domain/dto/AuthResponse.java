package com.whu.treehole.domain.dto;

/* 登录或注册成功后返回 token 与当前用户信息。 */

public record AuthResponse(
        String token,
        AuthUserDto user
) {
}
