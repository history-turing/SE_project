package com.whu.treehole.infra.model;

import lombok.Data;

@Data
public class UserRoleData {
    private Long userId;
    private String userCode;
    private String username;
    private String name;
    private String accountStatus;
    private Long roleId;
    private String roleCode;
    private String roleName;
}
