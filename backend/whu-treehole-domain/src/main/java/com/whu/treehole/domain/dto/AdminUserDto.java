package com.whu.treehole.domain.dto;

import com.whu.treehole.domain.enums.AccountStatus;
import java.util.List;

public record AdminUserDto(
        Long id,
        String userCode,
        String username,
        String name,
        AccountStatus accountStatus,
        List<RoleDto> roles
) {
}
