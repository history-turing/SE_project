package com.whu.treehole.infra.model;

/* 账号凭证数据承载用户名、邮箱和密码摘要。 */

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AuthCredentialData {
    private Long id;
    private Long userId;
    private String email;
    private String username;
    private String passwordHash;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
