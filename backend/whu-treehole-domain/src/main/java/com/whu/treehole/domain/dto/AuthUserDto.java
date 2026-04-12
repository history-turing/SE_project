package com.whu.treehole.domain.dto;

/* Current authenticated user summary. */

import com.whu.treehole.domain.enums.AccountStatus;
import java.util.List;

public record AuthUserDto(
        Long id,
        String userCode,
        String username,
        String email,
        String name,
        String avatar,
        List<RoleDto> roles,
        List<PermissionDto> permissions,
        AccountStatus accountStatus
) {
}
