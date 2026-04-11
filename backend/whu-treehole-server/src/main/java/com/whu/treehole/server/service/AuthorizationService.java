package com.whu.treehole.server.service;

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.enums.AccountStatus;
import com.whu.treehole.infra.mapper.AuthMapper;
import com.whu.treehole.server.support.AuthContextHolder;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    private final AuthMapper authMapper;

    public AuthorizationService(AuthMapper authMapper) {
        this.authMapper = authMapper;
    }

    public void assertCanWrite(long userId, String permissionCode) {
        assertActiveUser(userId);
        assertPermission(userId, permissionCode);
    }

    public void assertActiveUser(long userId) {
        if (resolveAccountStatus(userId) == AccountStatus.BANNED) {
            throw new BusinessException(4031, "USER_BANNED");
        }
    }

    public void assertPermission(long userId, String permissionCode) {
        if (!hasPermission(userId, permissionCode)) {
            throw new BusinessException(4030, "FORBIDDEN");
        }
    }

    public boolean hasPermission(long userId, String permissionCode) {
        if (permissionCode == null || permissionCode.isBlank()) {
            return true;
        }
        return authMapper.selectPermissionsByUserId(userId).stream()
                .anyMatch(permission -> permissionCode.equals(permission.getCode()));
    }

    public AccountStatus resolveAccountStatus(long userId) {
        if (AuthContextHolder.isCurrentUser(userId)) {
            AccountStatus contextStatus = AuthContextHolder.peekAccountStatus();
            if (contextStatus != null) {
                return contextStatus;
            }
        }
        return parseAccountStatus(authMapper.selectAccountStatusByUserId(userId));
    }

    private AccountStatus parseAccountStatus(String accountStatusCode) {
        if (accountStatusCode == null || accountStatusCode.isBlank()) {
            return AccountStatus.ACTIVE;
        }
        try {
            return AccountStatus.valueOf(accountStatusCode.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return AccountStatus.ACTIVE;
        }
    }
}
