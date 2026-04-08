# 部署说明

## 1. 运行依赖
- JDK 17
- Maven 3.9+
- MySQL 8.x
- Redis 7.x
- 可选: SMTP 邮箱服务

## 2. 关键配置文件
- `backend/whu-treehole-server/src/main/resources/application.yml`
  - MySQL、Redis、SMTP、认证时效主配置
- `backend/whu-treehole-server/src/main/resources/application-local.yml`
  - 本地 SQL 初始化配置

## 3. 常用环境变量
| 变量名 | 说明 |
|--------|------|
| `TREEHOLE_SERVER_PORT` | 服务端口 |
| `TREEHOLE_REDIS_HOST` / `TREEHOLE_REDIS_PORT` | Redis 地址与端口 |
| `TREEHOLE_MAIL_HOST` / `TREEHOLE_MAIL_PORT` | SMTP 地址与端口 |
| `TREEHOLE_MAIL_USERNAME` / `TREEHOLE_MAIL_PASSWORD` | SMTP 账号密码 |
| `TREEHOLE_AUTH_MAIL_FROM` | 发件人地址，默认取 SMTP 用户名 |
| `TREEHOLE_AUTH_EMAIL_CODE_TTL` | 验证码有效期，默认 5m |
| `TREEHOLE_AUTH_EMAIL_SEND_COOLDOWN` | 发码冷却时间，默认 60s |
| `TREEHOLE_AUTH_SESSION_TTL` | 登录会话有效期，默认 7d |
| `TREEHOLE_AUTH_MOCK_EMAIL_ENABLED` | 本地是否启用日志模拟验证码 |
| `TREEHOLE_SQL_INIT_MODE` | SQL 初始化模式，本地建议 `always` |

## 4. 构建
```bash
mvn -q -s settings.xml -pl whu-treehole-server -am package -DskipTests
```

## 5. 启动
```bash
java -jar whu-treehole-server/target/whu-treehole-server-0.1.0-SNAPSHOT.jar --spring.profiles.active=local
```

## 6. 本地联调建议
1. 本地 MySQL、Redis 启动后，将 `TREEHOLE_SQL_INIT_MODE=always`。
2. 若尚未配置 SMTP，可将 `TREEHOLE_AUTH_MOCK_EMAIL_ENABLED=true`，验证码会打印到后端日志。
3. 启动后端，再启动前端 `npm run dev`。
4. 首次访问先进入 `/register`，完成邮箱验证码注册，再使用用户名密码登录。

## 7. 真实邮箱发送说明
- 要真实发信，必须配置 `TREEHOLE_MAIL_HOST`、`TREEHOLE_MAIL_PORT`、`TREEHOLE_MAIL_USERNAME`、`TREEHOLE_MAIL_PASSWORD`。
- 常见 SMTP 还需要 TLS/STARTTLS，默认已在 `application.yml` 中开启。
- 如果 `/actuator/health` 显示 mail 不健康，而你启用了 mock 模式，可忽略该项对本地联调的影响。
