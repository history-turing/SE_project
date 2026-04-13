package com.whu.treehole.server.service;

/* 认证服务负责验证码发送、注册、登录与会话管理。 */

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.dto.AuthResponse;
import com.whu.treehole.domain.dto.AuthUserDto;
import com.whu.treehole.domain.dto.EmailCodeRequest;
import com.whu.treehole.domain.dto.LoginRequest;
import com.whu.treehole.domain.dto.PermissionDto;
import com.whu.treehole.domain.dto.RegisterRequest;
import com.whu.treehole.domain.dto.RoleDto;
import com.whu.treehole.domain.enums.AccountStatus;
import com.whu.treehole.infra.mapper.AuthMapper;
import com.whu.treehole.infra.mapper.PortalQueryMapper;
import com.whu.treehole.infra.model.AuthCredentialData;
import com.whu.treehole.infra.model.PermissionData;
import com.whu.treehole.infra.model.RoleData;
import com.whu.treehole.infra.model.UserCreateData;
import com.whu.treehole.infra.model.UserProfileData;
import com.whu.treehole.server.config.AuthProperties;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthMapper authMapper;
    private final PortalQueryMapper portalQueryMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties authProperties;
    private final MailProperties mailProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(AuthMapper authMapper,
                       PortalQueryMapper portalQueryMapper,
                       StringRedisTemplate stringRedisTemplate,
                       JavaMailSender javaMailSender,
                       PasswordEncoder passwordEncoder,
                       AuthProperties authProperties,
                       MailProperties mailProperties) {
        this.authMapper = authMapper;
        this.portalQueryMapper = portalQueryMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.javaMailSender = javaMailSender;
        this.passwordEncoder = passwordEncoder;
        this.authProperties = authProperties;
        this.mailProperties = mailProperties;
    }

    public void sendEmailCode(EmailCodeRequest request) {
        String email = normalizeEmail(request.email());
        validateWhuEmail(email);

        if (authMapper.selectCredentialByEmail(email) != null) {
            throw new BusinessException(4008, "该邮箱已完成注册，请直接使用用户名和密码登录");
        }
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(emailSendLockKey(email)))) {
            throw new BusinessException(4009, "验证码发送过于频繁，请稍后再试");
        }

        String code = generateEmailCode();
        stringRedisTemplate.opsForValue().set(emailCodeKey(email), code, authProperties.getEmailCodeTtl());
        stringRedisTemplate.opsForValue().set(emailSendLockKey(email), "1", authProperties.getEmailSendCooldown());

        if (authProperties.isMockEmailEnabled()) {
            log.info("邮箱验证码模拟发送成功: email={}, code={}", email, code);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        String fromAddress = resolveMailFrom();
        if (!fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setTo(email);
        message.setSubject("武大树洞注册验证码");
        message.setText(buildEmailContent(code));
        try {
            javaMailSender.send(message);
        } catch (MailException exception) {
            stringRedisTemplate.delete(emailCodeKey(email));
            stringRedisTemplate.delete(emailSendLockKey(email));
            log.error("邮箱验证码发送失败: email={}, host={}, port={}, username={}, from={}",
                    email,
                    mailProperties.getHost(),
                    mailProperties.getPort(),
                    mailProperties.getUsername(),
                    fromAddress,
                    exception);
            throw new BusinessException(5001, "验证码发送失败，请检查邮箱服务配置");
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        String username = normalizeUsername(request.username());
        validateWhuEmail(email);

        String cachedCode = stringRedisTemplate.opsForValue().get(emailCodeKey(email));
        if (cachedCode == null || !cachedCode.equals(request.code().trim())) {
            throw new BusinessException(4007, "验证码错误或已过期");
        }
        if (authMapper.selectCredentialByEmail(email) != null) {
            throw new BusinessException(4008, "该邮箱已完成注册，请直接登录");
        }
        if (authMapper.selectCredentialByUsername(username) != null) {
            throw new BusinessException(4012, "用户名已被占用");
        }

        LocalDateTime now = LocalDateTime.now();
        UserCreateData userCreateData = buildDefaultUser(username, now);
        authMapper.insertUser(userCreateData);

        AuthCredentialData credential = new AuthCredentialData();
        credential.setUserId(userCreateData.getId());
        credential.setEmail(email);
        credential.setUsername(username);
        credential.setPasswordHash(passwordEncoder.encode(request.password().trim()));
        credential.setEmailVerifiedAt(now);
        credential.setLastLoginAt(now);
        credential.setCreatedAt(now);
        credential.setUpdatedAt(now);
        authMapper.insertCredential(credential);

        authMapper.insertUserBadge(userCreateData.getId(), "邮箱已认证", 1);
        authMapper.insertUserStat(userCreateData.getId(), "已发树洞", "0", 1);
        authMapper.insertUserStat(userCreateData.getId(), "收藏内容", "0", 2);
        authMapper.insertUserStat(userCreateData.getId(), "已建立私信", "0", 3);

        ensureDefaultUserRole(userCreateData.getId(), now);
        stringRedisTemplate.delete(emailCodeKey(email));
        stringRedisTemplate.delete(emailSendLockKey(email));
        return buildAuthResponse(userCreateData.getId());
    }

    public AuthResponse login(LoginRequest request) {
        String username = normalizeUsername(request.username());
        AuthCredentialData credential = authMapper.selectCredentialByUsername(username);
        if (credential == null || !passwordEncoder.matches(request.password().trim(), credential.getPasswordHash())) {
            throw new BusinessException(4013, "用户名或密码错误");
        }

        LocalDateTime now = LocalDateTime.now();
        authMapper.updateLastLoginAt(credential.getUserId(), now);
        return buildAuthResponse(credential.getUserId());
    }

    public AuthUserDto getCurrentUser(long userId) {
        ensureDefaultUserRole(userId, LocalDateTime.now());
        AuthCredentialData credential = authMapper.selectCredentialByUserId(userId);
        if (credential == null) {
            throw new BusinessException(4011, "登录已失效，请重新登录");
        }

        UserProfileData profile = portalQueryMapper.selectUserProfile(userId);
        if (profile == null) {
            throw new BusinessException(4040, "未找到当前用户资料");
        }

        List<RoleDto> roles = authMapper.selectRolesByUserId(userId).stream()
                .map(this::toRoleDto)
                .toList();
        List<PermissionDto> permissions = authMapper.selectPermissionsByUserId(userId).stream()
                .map(this::toPermissionDto)
                .toList();
        AccountStatus accountStatus = parseAccountStatus(authMapper.selectAccountStatusByUserId(userId));

        return new AuthUserDto(
                profile.getId(),
                profile.getUserCode(),
                credential.getUsername(),
                credential.getEmail(),
                profile.getName(),
                profile.getAvatarUrl(),
                roles,
                permissions,
                accountStatus
        );
    }

    public void logout(String token) {
        stringRedisTemplate.delete(sessionKey(token));
    }

    private AuthResponse buildAuthResponse(long userId) {
        String token = generateToken();
        stringRedisTemplate.opsForValue().set(sessionKey(token), String.valueOf(userId), authProperties.getSessionTtl());
        return new AuthResponse(token, getCurrentUser(userId));
    }

    private void ensureDefaultUserRole(long userId, LocalDateTime now) {
        if (!authMapper.selectRolesByUserId(userId).isEmpty()) {
            return;
        }

        RoleData defaultRole = authMapper.selectRoleByCode("USER");
        if (defaultRole == null || defaultRole.getId() == null) {
            throw new BusinessException(5002, "DEFAULT_USER_ROLE_MISSING");
        }

        authMapper.insertUserRole(userId, defaultRole.getId(), now, userId);
    }

    private UserCreateData buildDefaultUser(String username, LocalDateTime now) {
        UserCreateData user = new UserCreateData();
        user.setUserCode("user-" + UUID.randomUUID().toString().replace("-", ""));
        user.setName(username);
        user.setTagline("刚完成武大邮箱认证，准备在树洞留下第一条记录。");
        user.setCollege("待完善");
        user.setGradeYear("新用户");
        user.setBio("这个用户还没有填写个人简介。");
        user.setAvatarUrl(generateAvatarDataUrl(username));
        user.setCreatedAt(now);
        return user;
    }

    private void validateWhuEmail(String email) {
        String suffix = authProperties.getEmailSuffix() == null ? "@whu.edu.cn" : authProperties.getEmailSuffix();
        if (!email.endsWith(suffix.toLowerCase())) {
            throw new BusinessException(4006, "仅支持武汉大学教育邮箱注册");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim();
    }

    private String generateEmailCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private String generateToken() {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String emailCodeKey(String email) {
        return "auth:email-code:" + email;
    }

    private String emailSendLockKey(String email) {
        return "auth:email-send-lock:" + email;
    }

    private String sessionKey(String token) {
        return "auth:session:" + token;
    }

    private String resolveMailFrom() {
        if (authProperties.getMailFrom() != null && !authProperties.getMailFrom().isBlank()) {
            return authProperties.getMailFrom().trim();
        }
        return mailProperties.getUsername() == null ? "" : mailProperties.getUsername().trim();
    }

    private String buildEmailContent(String code) {
        return "您正在注册武大树洞账号。\n\n验证码: " + code
                + "\n有效期: " + authProperties.getEmailCodeTtl().toMinutes() + " 分钟。"
                + "\n如果不是您本人操作，请忽略本邮件。";
    }

    private String generateAvatarDataUrl(String username) {
        String initials = username.isBlank() ? "WHU" : username.substring(0, Math.min(2, username.length())).toUpperCase();
        String svg = "<svg xmlns='http://www.w3.org/2000/svg' width='160' height='160' viewBox='0 0 160 160'>"
                + "<rect width='160' height='160' rx='36' fill='#B85A73'/>"
                + "<text x='50%' y='54%' text-anchor='middle' font-size='52' font-family='Arial' fill='white'>"
                + initials + "</text></svg>";
        return "data:image/svg+xml," + URLEncoder.encode(svg, StandardCharsets.UTF_8);
    }

    private RoleDto toRoleDto(RoleData roleData) {
        return new RoleDto(roleData.getId(), roleData.getCode(), roleData.getName());
    }

    private PermissionDto toPermissionDto(PermissionData permissionData) {
        return new PermissionDto(permissionData.getId(), permissionData.getCode(), permissionData.getName());
    }

    private AccountStatus parseAccountStatus(String statusCode) {
        if (statusCode == null || statusCode.isBlank()) {
            return AccountStatus.ACTIVE;
        }
        try {
            return AccountStatus.valueOf(statusCode.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown account status: {}, fallback to ACTIVE", statusCode);
            return AccountStatus.ACTIVE;
        }
    }
}
