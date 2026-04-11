package com.whu.treehole.infra.model;

/* Permission data object for mapper query results. */
import lombok.Data;

@Data
public class PermissionData {
    private Long id;
    private String code;
    private String name;
}
