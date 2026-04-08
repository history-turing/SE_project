package com.whu.treehole.server.config;

/* 认证配置统一管理邮箱域名、验证码和会话时效。 */

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "treehole.auth")
public class AuthProperties {

    private String emailSuffix = "@whu.edu.cn";
    private Duration emailCodeTtl = Duration.ofMinutes(5);
    private Duration emailSendCooldown = Duration.ofSeconds(60);
    private Duration sessionTtl = Duration.ofDays(7);
    private boolean mockEmailEnabled = false;
    private String mailFrom = "";

    public String getEmailSuffix() {
        return emailSuffix;
    }

    public void setEmailSuffix(String emailSuffix) {
        this.emailSuffix = emailSuffix;
    }

    public Duration getEmailCodeTtl() {
        return emailCodeTtl;
    }

    public void setEmailCodeTtl(Duration emailCodeTtl) {
        this.emailCodeTtl = emailCodeTtl;
    }

    public Duration getEmailSendCooldown() {
        return emailSendCooldown;
    }

    public void setEmailSendCooldown(Duration emailSendCooldown) {
        this.emailSendCooldown = emailSendCooldown;
    }

    public Duration getSessionTtl() {
        return sessionTtl;
    }

    public void setSessionTtl(Duration sessionTtl) {
        this.sessionTtl = sessionTtl;
    }

    public boolean isMockEmailEnabled() {
        return mockEmailEnabled;
    }

    public void setMockEmailEnabled(boolean mockEmailEnabled) {
        this.mockEmailEnabled = mockEmailEnabled;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }
}
