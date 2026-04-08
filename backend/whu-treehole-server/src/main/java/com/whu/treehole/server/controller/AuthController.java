package com.whu.treehole.server.controller;

/* 认证控制器提供验证码、注册、登录和会话接口。 */

import com.whu.treehole.common.api.ApiResponse;
import com.whu.treehole.domain.dto.AuthResponse;
import com.whu.treehole.domain.dto.AuthUserDto;
import com.whu.treehole.domain.dto.EmailCodeRequest;
import com.whu.treehole.domain.dto.LoginRequest;
import com.whu.treehole.domain.dto.RegisterRequest;
import com.whu.treehole.server.service.AuthService;
import com.whu.treehole.server.support.AuthContextHolder;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/email-code")
    public ApiResponse<String> sendEmailCode(@Valid @RequestBody EmailCodeRequest request) {
        authService.sendEmailCode(request);
        return ApiResponse.success("验证码已发送");
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<AuthUserDto> me() {
        return ApiResponse.success(authService.getCurrentUser(AuthContextHolder.currentUserId()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout(AuthContextHolder.currentToken());
        return ApiResponse.success(null);
    }
}
