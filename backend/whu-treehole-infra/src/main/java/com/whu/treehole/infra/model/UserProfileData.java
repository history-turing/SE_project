package com.whu.treehole.infra.model;

/* 用户资料主表字段用于个人页基础信息展示。 */

import lombok.Data;

@Data
public class UserProfileData {
    private Long id;
    private String userCode;
    private String name;
    private String tagline;
    private String college;
    private String gradeYear;
    private String bio;
    private String avatarUrl;
}
