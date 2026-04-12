package com.whu.treehole.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.whu.treehole.domain.dto.AuthResponse;
import com.whu.treehole.domain.dto.LoginRequest;
import com.whu.treehole.domain.dto.AuthUserDto;
import com.whu.treehole.domain.enums.AccountStatus;
import com.whu.treehole.infra.mapper.AuthMapper;
import com.whu.treehole.infra.mapper.PortalQueryMapper;
import com.whu.treehole.infra.model.AuthCredentialData;
import com.whu.treehole.infra.model.PermissionData;
import com.whu.treehole.infra.model.RoleData;
import com.whu.treehole.infra.model.UserProfileData;
import com.whu.treehole.server.config.AuthProperties;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthMapper authMapper;

    @Mock
    private PortalQueryMapper portalQueryMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private AuthProperties authProperties;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(authProperties.getSessionTtl()).thenReturn(Duration.ofDays(7));
        authService = new AuthService(
                authMapper,
                portalQueryMapper,
                stringRedisTemplate,
                null,
                new BCryptPasswordEncoder(),
                authProperties,
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

    @Test
    void shouldLoginWithSeededCodexPassword() {
        AuthCredentialData credentialData = new AuthCredentialData();
        credentialData.setUserId(7L);
        credentialData.setUsername("codex-super");
        credentialData.setEmail("codex-super@whu.edu.cn");
        credentialData.setPasswordHash("$2a$10$F6NFYwJ4okPAS4BcNi5eYOB2ZZtCYgr4XBdpA3Ov3XQ5Q21ihChZK");

        UserProfileData profileData = new UserProfileData();
        profileData.setId(7L);
        profileData.setUserCode("codex-super");
        profileData.setName("codex-super");
        profileData.setAvatarUrl("https://example.com/avatar/codex-super.jpg");

        RoleData roleData = new RoleData();
        roleData.setId(10L);
        roleData.setCode("SUPER_ADMIN");
        roleData.setName("Super Admin");

        when(authMapper.selectCredentialByUsername("codex-super")).thenReturn(credentialData);
        when(authMapper.selectCredentialByUserId(7L)).thenReturn(credentialData);
        when(portalQueryMapper.selectUserProfile(7L)).thenReturn(profileData);
        when(authMapper.selectRolesByUserId(7L)).thenReturn(List.of(roleData));
        when(authMapper.selectPermissionsByUserId(7L)).thenReturn(List.of());
        when(authMapper.selectAccountStatusByUserId(7L)).thenReturn(AccountStatus.ACTIVE.name());

        AuthResponse response = authService.login(new LoginRequest("codex-super", "codex123"));

        assertNotNull(response);
        assertNotNull(response.token());
        assertEquals("codex-super", response.user().username());
        assertEquals("SUPER_ADMIN", response.user().roles().get(0).code());
    }
}
