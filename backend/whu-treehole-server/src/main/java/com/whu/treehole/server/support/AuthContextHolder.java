package com.whu.treehole.server.support;

/* 认证上下文在单次请求内保存当前用户与 token。 */

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.enums.AccountStatus;

public final class AuthContextHolder {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_TOKEN = new ThreadLocal<>();
    private static final ThreadLocal<AccountStatus> CURRENT_ACCOUNT_STATUS = new ThreadLocal<>();

    private AuthContextHolder() {
    }

    public static void set(long userId, String token, AccountStatus accountStatus) {
        CURRENT_USER_ID.set(userId);
        CURRENT_TOKEN.set(token);
        CURRENT_ACCOUNT_STATUS.set(accountStatus);
    }

    public static long currentUserId() {
        Long userId = CURRENT_USER_ID.get();
        if (userId == null) {
            throw new BusinessException(4010, "请先登录");
        }
        return userId;
    }

    public static String currentToken() {
        String token = CURRENT_TOKEN.get();
        if (token == null || token.isBlank()) {
            throw new BusinessException(4010, "请先登录");
        }
        return token;
    }

    public static AccountStatus currentAccountStatus() {
        AccountStatus accountStatus = CURRENT_ACCOUNT_STATUS.get();
        if (accountStatus == null) {
            throw new BusinessException(4010, "请先登录");
        }
        return accountStatus;
    }

    public static boolean isCurrentUser(long userId) {
        Long currentUserId = CURRENT_USER_ID.get();
        return currentUserId != null && currentUserId == userId;
    }

    public static AccountStatus peekAccountStatus() {
        return CURRENT_ACCOUNT_STATUS.get();
    }

    public static void clear() {
        CURRENT_USER_ID.remove();
        CURRENT_TOKEN.remove();
        CURRENT_ACCOUNT_STATUS.remove();
    }
}
