package com.whu.treehole.infra.model;

/* 注册时写入用户资料主表的默认字段。 */

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserCreateData {
    private Long id;
    private String userCode;
    private String name;
    private String tagline;
    private String college;
    private String gradeYear;
    private String bio;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
