package com.whu.treehole.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.whu.treehole.domain.dto.AuthUserDto;
import com.whu.treehole.domain.enums.AccountStatus;
import com.whu.treehole.infra.mapper.AuthMapper;
import com.whu.treehole.infra.mapper.PortalQueryMapper;
import com.whu.treehole.infra.model.AuthCredentialData;
import com.whu.treehole.infra.model.PermissionData;
import com.whu.treehole.infra.model.RoleData;
import com.whu.treehole.infra.model.UserProfileData;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthMapper authMapper;

    @Mock
    private PortalQueryMapper portalQueryMapper;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                authMapper,
                portalQueryMapper,
                null,
                null,
                null,
                null,
                null);
    }

    @Test
    void shouldReturnRolesPermissionsAndAccountStatusWhenGettingCurrentUser() {
        AuthCredentialData credentialData = new AuthCredentialData();
        credentialData.setUserId(1L);
        credentialData.setUsername("xiewei");
        credentialData.setEmail("xiewei@whu.edu.cn");

        UserProfileData profileData = new UserProfileData();
        profileData.setId(1L);
        profileData.setName("xiewei");
        profileData.setAvatarUrl("https://example.com/avatar.jpg");

        RoleData roleData = new RoleData();
        roleData.setId(10L);
        roleData.setCode("SUPER_ADMIN");
        roleData.setName("Super Admin");

        PermissionData permissionData = new PermissionData();
        permissionData.setId(100L);
        permissionData.setCode("post.create");
        permissionData.setName("Create Post");

        when(authMapper.selectCredentialByUserId(1L)).thenReturn(credentialData);
        when(portalQueryMapper.selectUserProfile(1L)).thenReturn(profileData);
        when(authMapper.selectRolesByUserId(1L)).thenReturn(List.of(roleData));
        when(authMapper.selectPermissionsByUserId(1L)).thenReturn(List.of(permissionData));
        when(authMapper.selectAccountStatusByUserId(1L)).thenReturn(AccountStatus.ACTIVE.name());

        AuthUserDto currentUser = authService.getCurrentUser(1L);

        assertNotNull(currentUser);
        assertEquals("xiewei", currentUser.username());
        assertEquals(1, currentUser.roles().size());
        assertEquals("SUPER_ADMIN", currentUser.roles().get(0).code());
        assertEquals(1, currentUser.permissions().size());
        assertEquals("post.create", currentUser.permissions().get(0).code());
        assertEquals(AccountStatus.ACTIVE, currentUser.accountStatus());
    }
}
