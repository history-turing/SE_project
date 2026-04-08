package com.whu.treehole.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/* 注册请求包含邮箱验证码与账号凭证。 */

public record RegisterRequest(
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        @Size(max = 128, message = "邮箱长度不能超过128个字符")
        String email,
        @NotBlank(message = "验证码不能为空")
        @Pattern(regexp = "\\d{6}", message = "验证码必须是6位数字")
        String code,
        @NotBlank(message = "用户名不能为空")
        @Pattern(regexp = "[A-Za-z0-9_]{3,20}", message = "用户名需为3到20位字母、数字或下划线")
        String username,
        @NotBlank(message = "密码不能为空")
        @Size(min = 8, max = 64, message = "密码长度需在8到64个字符之间")
        String password
) {
}
