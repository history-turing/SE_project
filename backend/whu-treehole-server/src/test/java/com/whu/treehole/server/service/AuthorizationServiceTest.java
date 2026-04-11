package com.whu.treehole.server.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.enums.AccountStatus;
import com.whu.treehole.infra.mapper.AuthMapper;
import com.whu.treehole.infra.model.PermissionData;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private AuthMapper authMapper;

    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = new AuthorizationService(authMapper);
    }

    @Test
    void shouldRejectWriteWhenUserIsBanned() {
        when(authMapper.selectAccountStatusByUserId(9L)).thenReturn(AccountStatus.BANNED.name());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authorizationService.assertCanWrite(9L, "post.create"));

        assertEquals(4031, exception.getCode());
        assertEquals("USER_BANNED", exception.getMessage());
    }

    @Test
    void shouldRejectWriteWhenPermissionMissing() {
        when(authMapper.selectAccountStatusByUserId(9L)).thenReturn(AccountStatus.ACTIVE.name());
        when(authMapper.selectPermissionsByUserId(9L)).thenReturn(List.of());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authorizationService.assertCanWrite(9L, "post.create"));

        assertEquals(4030, exception.getCode());
        assertEquals("FORBIDDEN", exception.getMessage());
    }

    @Test
    void shouldAllowWriteWhenUserIsActiveAndHasPermission() {
        PermissionData permissionData = new PermissionData();
        permissionData.setCode("post.create");

        when(authMapper.selectAccountStatusByUserId(9L)).thenReturn(AccountStatus.ACTIVE.name());
        when(authMapper.selectPermissionsByUserId(9L)).thenReturn(List.of(permissionData));

        assertDoesNotThrow(() -> authorizationService.assertCanWrite(9L, "post.create"));
    }
}
