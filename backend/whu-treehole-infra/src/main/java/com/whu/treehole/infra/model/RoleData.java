package com.whu.treehole.infra.model;

/* Role data object for mapper query results. */
import lombok.Data;

@Data
public class RoleData {
    private Long id;
    private String code;
    private String name;
}
