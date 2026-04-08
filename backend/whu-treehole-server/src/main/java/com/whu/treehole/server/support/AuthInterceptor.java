package com.whu.treehole.server.support;

/* 认证拦截器负责从 Bearer Token 恢复当前登录用户。 */

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.server.config.AuthProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final StringRedisTemplate stringRedisTemplate;
    private final AuthProperties authProperties;

    public AuthInterceptor(StringRedisTemplate stringRedisTemplate, AuthProperties authProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.authProperties = authProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(4010, "请先登录");
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new BusinessException(4010, "请先登录");
        }

        String sessionValue = stringRedisTemplate.opsForValue().get(sessionKey(token));
        if (sessionValue == null || sessionValue.isBlank()) {
            throw new BusinessException(4011, "登录已失效，请重新登录");
        }

        long userId;
        try {
            userId = Long.parseLong(sessionValue);
        } catch (NumberFormatException exception) {
            throw new BusinessException(4011, "登录已失效，请重新登录");
        }

        stringRedisTemplate.expire(sessionKey(token), authProperties.getSessionTtl());
        AuthContextHolder.set(userId, token);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContextHolder.clear();
    }

    private String sessionKey(String token) {
        return "auth:session:" + token;
    }
}
