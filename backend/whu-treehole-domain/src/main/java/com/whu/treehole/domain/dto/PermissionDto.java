package com.whu.treehole.domain.dto;

/* Permission summary for auth contract. */
public record PermissionDto(
        Long id,
        String code,
        String name
) {
}
