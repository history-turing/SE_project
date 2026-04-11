package com.whu.treehole.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleAssignmentRequest(
        @NotBlank(message = "角色编码不能为空")
        String roleCode
) {
}
