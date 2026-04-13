package com.whu.treehole.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.whu.treehole.domain.dto.AuthResponse;
import com.whu.treehole.domain.dto.LoginRequest;
import com.whu.treehole.domain.dto.AuthUserDto;
import com.whu.treehole.domain.dto.RegisterRequest;
import com.whu.treehole.domain.enums.AccountStatus;
import com.whu.treehole.infra.mapper.AuthMapper;
import com.whu.treehole.infra.mapper.PortalQueryMapper;
import com.whu.treehole.infra.model.AuthCredentialData;
import com.whu.treehole.infra.model.PermissionData;
import com.whu.treehole.infra.model.RoleData;
import com.whu.treehole.infra.model.UserCreateData;
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
    void shouldLoginWithSeededXieweiPassword() {
        AuthCredentialData credentialData = new AuthCredentialData();
        credentialData.setUserId(1L);
        credentialData.setUsername("xiewei");
        credentialData.setEmail("xiewei@whu.edu.cn");
        credentialData.setPasswordHash("$2a$10$HX6zs3CimxyK5wGQ5VQc.OEzghS2H85ooidaxrV9VSpaX2Rl/EKc2");

        UserProfileData profileData = new UserProfileData();
        profileData.setId(1L);
        profileData.setUserCode("xiewei");
        profileData.setName("xiewei");
        profileData.setAvatarUrl("https://example.com/avatar/xiewei.jpg");

        RoleData roleData = new RoleData();
        roleData.setId(10L);
        roleData.setCode("SUPER_ADMIN");
        roleData.setName("Super Admin");

        when(authMapper.selectCredentialByUsername("xiewei")).thenReturn(credentialData);
        when(authMapper.selectCredentialByUserId(1L)).thenReturn(credentialData);
        when(portalQueryMapper.selectUserProfile(1L)).thenReturn(profileData);
        when(authMapper.selectRolesByUserId(1L)).thenReturn(List.of(roleData));
        when(authMapper.selectPermissionsByUserId(1L)).thenReturn(List.of());
        when(authMapper.selectAccountStatusByUserId(1L)).thenReturn(AccountStatus.ACTIVE.name());

        AuthResponse response = authService.login(new LoginRequest("xiewei", "xiewei123"));

        assertNotNull(response);
        assertNotNull(response.token());
        assertEquals("xiewei", response.user().username());
        assertEquals("SUPER_ADMIN", response.user().roles().get(0).code());
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

    @Test
    void shouldAssignUserRoleWhenRegisteringNewAccount() {
        when(valueOperations.get("auth:email-code:new-user@whu.edu.cn")).thenReturn("123456");
        when(authMapper.selectCredentialByEmail("new-user@whu.edu.cn")).thenReturn(null);
        when(authMapper.selectCredentialByUsername("new-user")).thenReturn(null);
        doAnswer(invocation -> {
            UserCreateData user = invocation.getArgument(0);
            user.setId(11L);
            return null;
        }).when(authMapper).insertUser(any(UserCreateData.class));

        RoleData userRole = new RoleData();
        userRole.setId(30L);
        userRole.setCode("USER");
        userRole.setName("User");

        AuthCredentialData credentialData = new AuthCredentialData();
        credentialData.setUserId(11L);
        credentialData.setUsername("new-user");
        credentialData.setEmail("new-user@whu.edu.cn");

        UserProfileData profileData = new UserProfileData();
        profileData.setId(11L);
        profileData.setUserCode("user-generated");
        profileData.setName("new-user");
        profileData.setAvatarUrl("https://example.com/avatar/new-user.jpg");

        PermissionData permissionData = new PermissionData();
        permissionData.setId(100L);
        permissionData.setCode("post.create");
        permissionData.setName("Create Post");

        when(authMapper.selectRoleByCode("USER")).thenReturn(userRole);
        when(authMapper.selectCredentialByUserId(11L)).thenReturn(credentialData);
        when(portalQueryMapper.selectUserProfile(11L)).thenReturn(profileData);
        when(authMapper.selectRolesByUserId(11L)).thenReturn(List.of(), List.of(userRole));
        when(authMapper.selectPermissionsByUserId(11L)).thenReturn(List.of(permissionData));
        when(authMapper.selectAccountStatusByUserId(11L)).thenReturn(AccountStatus.ACTIVE.name());

        AuthResponse response = authService.register(
                new RegisterRequest("new-user@whu.edu.cn", "123456", "new-user", "password123"));

        assertNotNull(response);
        assertEquals("USER", response.user().roles().get(0).code());
        assertEquals("post.create", response.user().permissions().get(0).code());
        verify(authMapper).insertUserRole(eq(11L), eq(30L), any(), eq(11L));
    }

    @Test
    void shouldHealMissingUserRoleOnLogin() {
        AuthCredentialData credentialData = new AuthCredentialData();
        credentialData.setUserId(12L);
        credentialData.setUsername("healed-user");
        credentialData.setEmail("healed-user@whu.edu.cn");
        credentialData.setPasswordHash(new BCryptPasswordEncoder().encode("password123"));

        UserProfileData profileData = new UserProfileData();
        profileData.setId(12L);
        profileData.setUserCode("user-healed");
        profileData.setName("healed-user");
        profileData.setAvatarUrl("https://example.com/avatar/healed-user.jpg");

        RoleData userRole = new RoleData();
        userRole.setId(30L);
        userRole.setCode("USER");
        userRole.setName("User");

        PermissionData permissionData = new PermissionData();
        permissionData.setId(100L);
        permissionData.setCode("post.create");
        permissionData.setName("Create Post");

        when(authMapper.selectCredentialByUsername("healed-user")).thenReturn(credentialData);
        when(authMapper.selectCredentialByUserId(12L)).thenReturn(credentialData);
        when(authMapper.selectRoleByCode("USER")).thenReturn(userRole);
        when(portalQueryMapper.selectUserProfile(12L)).thenReturn(profileData);
        when(authMapper.selectRolesByUserId(12L)).thenReturn(List.of(), List.of(userRole));
        when(authMapper.selectPermissionsByUserId(12L)).thenReturn(List.of(permissionData));
        when(authMapper.selectAccountStatusByUserId(12L)).thenReturn(AccountStatus.ACTIVE.name());

        AuthResponse response = authService.login(new LoginRequest("healed-user", "password123"));

        assertNotNull(response);
        assertEquals("USER", response.user().roles().get(0).code());
        verify(authMapper).insertUserRole(eq(12L), eq(30L), any(), eq(12L));
    }
}
