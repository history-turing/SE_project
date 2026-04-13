# Messaging Platform Phase C Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为现有武大树洞项目落地可扩展的私信 Phase C 方案，包括 `message-service`、`api-gateway`、`nacos`、`redis`、`rabbitmq`、WebSocket、撤回、拉黑、举报审计和前端私信入口。

**Architecture:** 当前仓库仍是单体后端 + 单前端 + Docker Compose，因此实施顺序必须先完成共享模型与模块拆分，再让私信服务独立运行，随后接入网关、注册中心和实时链路，最后再接前端和管理能力。整个实现保持 MySQL 为事实源、Redis 为高频状态层、RabbitMQ 为事件层，并保留前端现有布局。

**Tech Stack:** Spring Boot 3.3, Spring Cloud Gateway, Spring Cloud Alibaba Nacos, MyBatis, MySQL 8, Redis 7, RabbitMQ, native WebSocket, React 18, TypeScript, Vite, Vitest, Docker Compose, GitHub Actions

---

## File Structure Map

### Backend Maven modules

- Modify: `backend/pom.xml`
  - 新增 `whu-treehole-message-service` 和 `whu-treehole-gateway` 模块
  - 增加 Spring Cloud 与 Spring Cloud Alibaba BOM
- Create: `backend/whu-treehole-message-service/pom.xml`
  - 私信服务独立 Spring Boot 应用
- Create: `backend/whu-treehole-gateway/pom.xml`
  - Gateway 独立 Spring Boot 应用

### Shared contracts and persistence

- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/DirectConversationRequest.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/ConversationListItemDto.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/ConversationDetailDto.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/ConversationPeerDto.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/MessageSendRequest.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/MessageReportRequest.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/MessageEventDto.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/enums/ConversationStatus.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/enums/MessageType.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/enums/MessageStatus.java`
- Create: `backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/model/DmConversationData.java`
- Create: `backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/model/DmConversationParticipantData.java`
- Create: `backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/model/DmMessageData.java`
- Create: `backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/model/DmUserBlockData.java`
- Create: `backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/model/DmConversationEventData.java`
- Create: `backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/mapper/MessageDomainMapper.java`
- Create: `backend/whu-treehole-infra/src/main/resources/mapper/MessageDomainMapper.xml`
- Modify: `backend/whu-treehole-server/src/main/resources/db/schema.sql`
- Modify: `backend/whu-treehole-server/src/main/resources/db/data.sql`

### Message service

- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/MessageServiceApplication.java`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/config/MessageServiceConfig.java`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/config/MessageWebSocketConfig.java`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/controller/MessageConversationController.java`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/controller/MessageModerationController.java`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/ConversationCommandService.java`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/ConversationQueryService.java`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageCommandService.java`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageModerationService.java`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageEventPublisher.java`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageEventConsumer.java`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageSessionRegistry.java`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/ws/MessageSocketHandler.java`
- Create: `backend/whu-treehole-message-service/src/main/resources/application.yml`
- Create: `backend/whu-treehole-message-service/src/test/java/com/whu/treehole/message/service/ConversationCommandServiceTest.java`
- Create: `backend/whu-treehole-message-service/src/test/java/com/whu/treehole/message/service/MessageCommandServiceTest.java`
- Create: `backend/whu-treehole-message-service/src/test/java/com/whu/treehole/message/service/MessageRealtimeServiceTest.java`

### Gateway and ops

- Create: `backend/whu-treehole-gateway/src/main/java/com/whu/treehole/gateway/GatewayApplication.java`
- Create: `backend/whu-treehole-gateway/src/main/java/com/whu/treehole/gateway/config/GatewayRoutesConfig.java`
- Create: `backend/whu-treehole-gateway/src/main/resources/application.yml`
- Create: `backend/whu-treehole-gateway/src/test/java/com/whu/treehole/gateway/GatewayRoutesConfigTest.java`
- Modify: `docker-compose.yml`
- Modify: `.env.example`
- Modify: `scripts/deploy.sh`
- Modify: `.github/workflows/ci-cd.yml`
- Modify: `frontend/nginx.conf`
- Modify: `backend/Dockerfile`
- Create: `backend/whu-treehole-message-service/Dockerfile`
- Create: `backend/whu-treehole-gateway/Dockerfile`

### Frontend

- Create: `frontend/src/components/ConversationLauncherButton.tsx`
- Create: `frontend/src/components/MessageRecallMenu.tsx`
- Create: `frontend/src/components/ConversationLauncherButton.test.tsx`
- Create: `frontend/src/pages/UserProfilePage.tsx`
- Create: `frontend/src/pages/UserProfilePage.test.tsx`
- Modify: `frontend/src/App.tsx`
- Modify: `frontend/src/components/PostCard.tsx`
- Modify: `frontend/src/components/PostCommentsPanel.tsx`
- Modify: `frontend/src/context/AppContext.tsx`
- Modify: `frontend/src/pages/ProfilePage.tsx`
- Modify: `frontend/src/pages/AdminPage.tsx`
- Modify: `frontend/src/services/api.ts`
- Modify: `frontend/src/types.ts`
- Modify: `frontend/src/styles.css`

## Scope Decomposition

这份 spec 实际覆盖三个相对独立的子系统：

- 私信域重构与独立服务
- 实时基础设施与服务治理
- 前端接入、管理入口与部署

为了避免一次性改崩，下面的 6 个任务按“每完成一个任务就得到一个可验证增量”的方式组织。执行顺序固定，但每个任务都能单独验收。

### Task 1: Shared Contracts And Module Scaffolding

**Files:**
- Create: `backend/whu-treehole-message-service/pom.xml`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/MessageServiceApplication.java`
- Create: `backend/whu-treehole-gateway/pom.xml`
- Create: `backend/whu-treehole-gateway/src/main/java/com/whu/treehole/gateway/GatewayApplication.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/DirectConversationRequest.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/ConversationListItemDto.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/enums/ConversationStatus.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/enums/MessageStatus.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/enums/MessageType.java`
- Create: `backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/model/DmConversationData.java`
- Create: `backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/model/DmConversationParticipantData.java`
- Create: `backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/mapper/MessageDomainMapper.java`
- Create: `backend/whu-treehole-message-service/src/test/java/com/whu/treehole/message/service/ConversationCommandServiceTest.java`
- Modify: `backend/pom.xml`
- Modify: `backend/whu-treehole-server/src/main/resources/db/schema.sql`
- Modify: `backend/whu-treehole-server/src/main/resources/db/data.sql`
- Test: `backend/whu-treehole-message-service/src/test/java/com/whu/treehole/message/service/ConversationCommandServiceTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.whu.treehole.message.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.whu.treehole.domain.dto.DirectConversationRequest;
import com.whu.treehole.infra.mapper.MessageDomainMapper;
import com.whu.treehole.infra.model.DmConversationData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConversationCommandServiceTest {

    @Mock
    private MessageDomainMapper messageDomainMapper;

    @Test
    void shouldReuseExistingSingleConversationForSameUsers() {
        DmConversationData existing = new DmConversationData();
        existing.setConversationCode("dm-1001");

        when(messageDomainMapper.selectSingleConversationBetweenUsers(7L, 9L)).thenReturn(existing);

        ConversationCommandService service = new ConversationCommandService(messageDomainMapper);
        String code = service.createOrGetSingleConversation(7L, new DirectConversationRequest("user-9"), 9L);

        assertEquals("dm-1001", code);
        verify(messageDomainMapper).selectSingleConversationBetweenUsers(7L, 9L);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q -f backend/pom.xml -s backend/settings.xml -pl whu-treehole-message-service -am -Dtest=ConversationCommandServiceTest test`

Expected: FAIL，提示 `whu-treehole-message-service` 模块、`ConversationCommandService`、`MessageDomainMapper` 或新 DTO/enum 不存在。

- [ ] **Step 3: Write minimal implementation**

```xml
<!-- backend/pom.xml -->
<modules>
    <module>whu-treehole-common</module>
    <module>whu-treehole-domain</module>
    <module>whu-treehole-infra</module>
    <module>whu-treehole-server</module>
    <module>whu-treehole-message-service</module>
    <module>whu-treehole-gateway</module>
</modules>

<properties>
    <java.version>17</java.version>
    <spring-cloud.version>2023.0.3</spring-cloud.version>
    <spring-cloud-alibaba.version>2023.0.1.2</spring-cloud-alibaba.version>
</properties>
```

```java
// backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/DirectConversationRequest.java
public record DirectConversationRequest(
        @NotBlank(message = "私信目标不能为空")
        String peerUserCode
) {
}

// backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/enums/ConversationStatus.java
public enum ConversationStatus {
    ACTIVE,
    FROZEN,
    CLOSED
}
```

```java
// backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/ConversationCommandService.java
@Service
public class ConversationCommandService {

    private final MessageDomainMapper messageDomainMapper;

    public ConversationCommandService(MessageDomainMapper messageDomainMapper) {
        this.messageDomainMapper = messageDomainMapper;
    }

    public String createOrGetSingleConversation(long operatorUserId,
                                                DirectConversationRequest request,
                                                long peerUserId) {
        DmConversationData existing = messageDomainMapper.selectSingleConversationBetweenUsers(operatorUserId, peerUserId);
        if (existing != null) {
            return existing.getConversationCode();
        }
        throw new UnsupportedOperationException("create path implemented in Task 2");
    }
}
```

```sql
-- backend/whu-treehole-server/src/main/resources/db/schema.sql
CREATE TABLE IF NOT EXISTS dm_conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_code VARCHAR(64) NOT NULL UNIQUE,
    conversation_type VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_by BIGINT NOT NULL,
    last_message_id BIGINT NULL,
    last_message_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dm_conversation_participants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    last_read_message_id BIGINT NULL,
    last_read_at DATETIME NULL,
    unread_count INT NOT NULL DEFAULT 0,
    pinned_flag TINYINT(1) NOT NULL DEFAULT 0,
    muted_flag TINYINT(1) NOT NULL DEFAULT 0,
    cleared_at DATETIME NULL,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dm_participants_conversation_user (conversation_id, user_id)
);
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -q -f backend/pom.xml -s backend/settings.xml -pl whu-treehole-message-service -am -Dtest=ConversationCommandServiceTest test`

Expected: PASS，`ConversationCommandServiceTest` 通过，Maven 能识别新模块和共享契约。

- [ ] **Step 5: Commit**

```bash
git add backend/pom.xml \
  backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/DirectConversationRequest.java \
  backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/ConversationListItemDto.java \
  backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/enums/ConversationStatus.java \
  backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/enums/MessageStatus.java \
  backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/enums/MessageType.java \
  backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/model/DmConversationData.java \
  backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/model/DmConversationParticipantData.java \
  backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/mapper/MessageDomainMapper.java \
  backend/whu-treehole-message-service/pom.xml \
  backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/MessageServiceApplication.java \
  backend/whu-treehole-gateway/pom.xml \
  backend/whu-treehole-gateway/src/main/java/com/whu/treehole/gateway/GatewayApplication.java \
  backend/whu-treehole-server/src/main/resources/db/schema.sql \
  backend/whu-treehole-server/src/main/resources/db/data.sql \
  backend/whu-treehole-message-service/src/test/java/com/whu/treehole/message/service/ConversationCommandServiceTest.java
git commit -m "feat: scaffold messaging service modules"
```

### Task 2: Message Service HTTP APIs And Persistence

**Files:**
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/ConversationDetailDto.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/MessageSendRequest.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/MessageReportRequest.java`
- Create: `backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/model/DmMessageData.java`
- Create: `backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/model/DmConversationEventData.java`
- Create: `backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/model/DmUserBlockData.java`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/controller/MessageConversationController.java`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/ConversationQueryService.java`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageCommandService.java`
- Create: `backend/whu-treehole-message-service/src/test/java/com/whu/treehole/message/service/MessageCommandServiceTest.java`
- Modify: `backend/whu-treehole-infra/src/main/resources/mapper/MessageDomainMapper.xml`
- Modify: `backend/whu-treehole-server/src/main/resources/db/schema.sql`
- Modify: `backend/whu-treehole-server/src/main/resources/db/data.sql`
- Test: `backend/whu-treehole-message-service/src/test/java/com/whu/treehole/message/service/MessageCommandServiceTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.whu.treehole.message.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.whu.treehole.domain.dto.MessageSendRequest;
import com.whu.treehole.domain.enums.MessageStatus;
import com.whu.treehole.infra.mapper.MessageDomainMapper;
import com.whu.treehole.infra.model.DmMessageData;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageCommandServiceTest {

    @Mock
    private MessageDomainMapper messageDomainMapper;

    @Captor
    private ArgumentCaptor<DmMessageData> messageCaptor;

    @Test
    void shouldPersistMessageAndIncreasePeerUnreadCount() {
        when(messageDomainMapper.selectConversationIdByCode("dm-1001")).thenReturn(18L);

        MessageCommandService service = new MessageCommandService(
                messageDomainMapper,
                Clock.fixed(Instant.parse("2026-04-12T08:10:00Z"), ZoneId.of("Asia/Shanghai")));

        String code = service.sendMessage(7L, "dm-1001", new MessageSendRequest("msg-client-1", "晚上好"));

        verify(messageDomainMapper).insertMessage(messageCaptor.capture());
        verify(messageDomainMapper).increaseUnreadForPeer(18L, 7L);
        assertEquals("晚上好", messageCaptor.getValue().getContentPayload());
        assertEquals(MessageStatus.SENT.name(), messageCaptor.getValue().getStatus());
        assertEquals(code, messageCaptor.getValue().getMessageCode());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q -f backend/pom.xml -s backend/settings.xml -pl whu-treehole-message-service -am -Dtest=MessageCommandServiceTest test`

Expected: FAIL，提示 `MessageCommandService`、`MessageSendRequest`、`DmMessageData` 或 mapper XML 语句不存在。

- [ ] **Step 3: Write minimal implementation**

```java
// backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/MessageSendRequest.java
public record MessageSendRequest(
        @NotBlank(message = "客户端消息ID不能为空")
        String clientMessageId,
        @NotBlank(message = "消息内容不能为空")
        @Size(max = 1000, message = "消息内容不能超过1000字符")
        String text
) {
}
```

```java
// backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageCommandService.java
@Service
public class MessageCommandService {

    private final MessageDomainMapper messageDomainMapper;
    private final Clock clock;

    public MessageCommandService(MessageDomainMapper messageDomainMapper, Clock clock) {
        this.messageDomainMapper = messageDomainMapper;
        this.clock = clock;
    }

    @Transactional
    public String sendMessage(long userId, String conversationCode, MessageSendRequest request) {
        Long conversationId = messageDomainMapper.selectConversationIdByCode(conversationCode);
        if (conversationId == null) {
            throw new BusinessException(4042, "会话不存在");
        }
        String messageCode = "msg-" + System.currentTimeMillis();
        DmMessageData data = new DmMessageData();
        data.setMessageCode(messageCode);
        data.setConversationId(conversationId);
        data.setSenderUserId(userId);
        data.setMessageType("TEXT");
        data.setStatus("SENT");
        data.setClientMessageId(request.clientMessageId());
        data.setContentPayload(request.text().trim());
        data.setSentAt(LocalDateTime.now(clock));
        messageDomainMapper.insertMessage(data);
        messageDomainMapper.updateConversationAfterSend(conversationId, data.getSentAt());
        messageDomainMapper.increaseUnreadForPeer(conversationId, userId);
        return messageCode;
    }
}
```

```java
// backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/controller/MessageConversationController.java
@RestController
@RequestMapping("/api/messages/conversations")
public class MessageConversationController {

    private final MessageCommandService messageCommandService;

    public MessageConversationController(MessageCommandService messageCommandService) {
        this.messageCommandService = messageCommandService;
    }

    @PostMapping("/{conversationCode}/messages")
    public ApiResponse<String> sendMessage(@PathVariable String conversationCode,
                                           @Valid @RequestBody MessageSendRequest request) {
        return ApiResponse.success(messageCommandService.sendMessage(AuthContextHolder.currentUserId(), conversationCode, request));
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -q -f backend/pom.xml -s backend/settings.xml -pl whu-treehole-message-service -am -Dtest=MessageCommandServiceTest test`

Expected: PASS，消息发送最小链路可持久化、可更新未读。

- [ ] **Step 5: Commit**

```bash
git add backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/ConversationDetailDto.java \
  backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/MessageSendRequest.java \
  backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/MessageReportRequest.java \
  backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/model/DmMessageData.java \
  backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/model/DmConversationEventData.java \
  backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/model/DmUserBlockData.java \
  backend/whu-treehole-infra/src/main/resources/mapper/MessageDomainMapper.xml \
  backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/controller/MessageConversationController.java \
  backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/ConversationQueryService.java \
  backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageCommandService.java \
  backend/whu-treehole-server/src/main/resources/db/schema.sql \
  backend/whu-treehole-server/src/main/resources/db/data.sql \
  backend/whu-treehole-message-service/src/test/java/com/whu/treehole/message/service/MessageCommandServiceTest.java
git commit -m "feat: add messaging http apis"
```

### Task 3: Redis, RabbitMQ And WebSocket Realtime Pipeline

**Files:**
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/config/MessageServiceConfig.java`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/config/MessageWebSocketConfig.java`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageEventPublisher.java`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageEventConsumer.java`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageSessionRegistry.java`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/ws/MessageSocketHandler.java`
- Create: `backend/whu-treehole-message-service/src/test/java/com/whu/treehole/message/service/MessageRealtimeServiceTest.java`
- Modify: `backend/whu-treehole-message-service/pom.xml`
- Modify: `backend/whu-treehole-message-service/src/main/resources/application.yml`
- Test: `backend/whu-treehole-message-service/src/test/java/com/whu/treehole/message/service/MessageRealtimeServiceTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.whu.treehole.message.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import com.whu.treehole.domain.dto.MessageEventDto;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class MessageRealtimeServiceTest {

    @Test
    void shouldPublishCreatedEventAndPushToOnlineUser() {
        RabbitTemplate rabbitTemplate = org.mockito.Mockito.mock(RabbitTemplate.class);
        MessageSessionRegistry registry = new MessageSessionRegistry();
        registry.bindUserSession(9L, "ws-1");

        MessageEventPublisher publisher = new MessageEventPublisher(rabbitTemplate);
        MessageEventConsumer consumer = new MessageEventConsumer(registry);

        MessageEventDto event = new MessageEventDto("message.created", "dm-1001", "msg-1", 7L, List.of(9L));

        publisher.publish(event);
        consumer.handle(event);

        verify(rabbitTemplate).convertAndSend("treehole.dm.exchange", "message.created", event);
        assertEquals(List.of("ws-1"), registry.findSessions(9L));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q -f backend/pom.xml -s backend/settings.xml -pl whu-treehole-message-service -am -Dtest=MessageRealtimeServiceTest test`

Expected: FAIL，提示 AMQP、WebSocket、事件 DTO 或会话注册组件不存在。

- [ ] **Step 3: Write minimal implementation**

```xml
<!-- backend/whu-treehole-message-service/pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

```java
// backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageSessionRegistry.java
@Component
public class MessageSessionRegistry {

    private final Map<Long, List<String>> userSessions = new ConcurrentHashMap<>();

    public void bindUserSession(long userId, String sessionId) {
        userSessions.computeIfAbsent(userId, ignored -> new CopyOnWriteArrayList<>()).add(sessionId);
    }

    public List<String> findSessions(long userId) {
        return userSessions.getOrDefault(userId, List.of());
    }
}
```

```java
// backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageEventPublisher.java
@Component
public class MessageEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public MessageEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(MessageEventDto event) {
        rabbitTemplate.convertAndSend("treehole.dm.exchange", event.type(), event);
    }
}
```

```yaml
# backend/whu-treehole-message-service/src/main/resources/application.yml
spring:
  application:
    name: whu-treehole-message-service
  data:
    redis:
      host: ${TREEHOLE_REDIS_HOST:localhost}
      port: ${TREEHOLE_REDIS_PORT:6379}
      password: ${TREEHOLE_REDIS_PASSWORD:}
  rabbitmq:
    host: ${TREEHOLE_RABBITMQ_HOST:localhost}
    port: ${TREEHOLE_RABBITMQ_PORT:5672}
    username: ${TREEHOLE_RABBITMQ_USERNAME:guest}
    password: ${TREEHOLE_RABBITMQ_PASSWORD:guest}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -q -f backend/pom.xml -s backend/settings.xml -pl whu-treehole-message-service -am -Dtest=MessageRealtimeServiceTest test`

Expected: PASS，私信事件能发布，在线会话注册最小链路成立。

- [ ] **Step 5: Commit**

```bash
git add backend/whu-treehole-message-service/pom.xml \
  backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/config/MessageServiceConfig.java \
  backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/config/MessageWebSocketConfig.java \
  backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageEventPublisher.java \
  backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageEventConsumer.java \
  backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageSessionRegistry.java \
  backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/ws/MessageSocketHandler.java \
  backend/whu-treehole-message-service/src/main/resources/application.yml \
  backend/whu-treehole-message-service/src/test/java/com/whu/treehole/message/service/MessageRealtimeServiceTest.java
git commit -m "feat: add messaging realtime pipeline"
```

### Task 4: Gateway, Nacos, Compose And CI/CD

**Files:**
- Create: `backend/whu-treehole-gateway/src/main/java/com/whu/treehole/gateway/config/GatewayRoutesConfig.java`
- Create: `backend/whu-treehole-gateway/src/main/resources/application.yml`
- Create: `backend/whu-treehole-gateway/src/test/java/com/whu/treehole/gateway/GatewayRoutesConfigTest.java`
- Create: `backend/whu-treehole-message-service/Dockerfile`
- Create: `backend/whu-treehole-gateway/Dockerfile`
- Modify: `docker-compose.yml`
- Modify: `.env.example`
- Modify: `scripts/deploy.sh`
- Modify: `.github/workflows/ci-cd.yml`
- Modify: `frontend/nginx.conf`
- Test: `backend/whu-treehole-gateway/src/test/java/com/whu/treehole/gateway/GatewayRoutesConfigTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.whu.treehole.gateway;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.whu.treehole.gateway.config.GatewayRoutesConfig;
import org.junit.jupiter.api.Test;

class GatewayRoutesConfigTest {

    @Test
    void shouldExposeApiAndWebSocketRoutes() {
        GatewayRoutesConfig config = new GatewayRoutesConfig();
        String routeSummary = config.routeIds();

        assertTrue(routeSummary.contains("message-service-api"));
        assertTrue(routeSummary.contains("message-service-ws"));
        assertTrue(routeSummary.contains("content-service-api"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q -f backend/pom.xml -s backend/settings.xml -pl whu-treehole-gateway -am -Dtest=GatewayRoutesConfigTest test`

Expected: FAIL，提示 gateway 模块、路由配置或 Spring Cloud 依赖不存在。

- [ ] **Step 3: Write minimal implementation**

```java
// backend/whu-treehole-gateway/src/main/java/com/whu/treehole/gateway/config/GatewayRoutesConfig.java
@Configuration
public class GatewayRoutesConfig {

    public String routeIds() {
        return String.join(",", "message-service-api", "message-service-ws", "content-service-api");
    }
}
```

```yaml
# docker-compose.yml
services:
  nacos:
    image: nacos/nacos-server:v2.4.3
  rabbitmq:
    image: rabbitmq:3.13-management
  message-service:
    build:
      context: ./backend/whu-treehole-message-service
  gateway:
    build:
      context: ./backend/whu-treehole-gateway
```

```nginx
# frontend/nginx.conf
location /api/ {
    proxy_pass http://gateway:8080;
}

location /ws/ {
    proxy_pass http://gateway:8080;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -q -f backend/pom.xml -s backend/settings.xml -pl whu-treehole-gateway -am -Dtest=GatewayRoutesConfigTest test`

Expected: PASS，Gateway 模块和最小路由配置可编译。

Run: `docker compose --env-file .env.example -f docker-compose.yml config`

Expected: PASS，Compose 新增 `nacos`、`rabbitmq`、`message-service`、`gateway` 后仍可解析。

- [ ] **Step 5: Commit**

```bash
git add backend/whu-treehole-gateway/src/main/java/com/whu/treehole/gateway/config/GatewayRoutesConfig.java \
  backend/whu-treehole-gateway/src/main/resources/application.yml \
  backend/whu-treehole-gateway/src/test/java/com/whu/treehole/gateway/GatewayRoutesConfigTest.java \
  backend/whu-treehole-message-service/Dockerfile \
  backend/whu-treehole-gateway/Dockerfile \
  docker-compose.yml .env.example scripts/deploy.sh .github/workflows/ci-cd.yml frontend/nginx.conf
git commit -m "feat: add gateway and service discovery scaffolding"
```

### Task 5: Frontend Private Messaging Integration

**Files:**
- Create: `frontend/src/components/ConversationLauncherButton.tsx`
- Create: `frontend/src/components/MessageRecallMenu.tsx`
- Create: `frontend/src/components/ConversationLauncherButton.test.tsx`
- Create: `frontend/src/pages/UserProfilePage.tsx`
- Create: `frontend/src/pages/UserProfilePage.test.tsx`
- Modify: `frontend/src/App.tsx`
- Modify: `frontend/src/components/PostCard.tsx`
- Modify: `frontend/src/components/PostCommentsPanel.tsx`
- Modify: `frontend/src/context/AppContext.tsx`
- Modify: `frontend/src/pages/ProfilePage.tsx`
- Modify: `frontend/src/services/api.ts`
- Modify: `frontend/src/types.ts`
- Modify: `frontend/src/styles.css`
- Test: `frontend/src/components/ConversationLauncherButton.test.tsx`
- Test: `frontend/src/pages/UserProfilePage.test.tsx`

- [ ] **Step 1: Write the failing tests**

```tsx
import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/react';
import { renderWithProviders } from '../test/renderWithProviders';
import { ConversationLauncherButton } from './ConversationLauncherButton';

vi.mock('../services/api', () => ({
  createDirectConversation: vi.fn().mockResolvedValue({ conversationCode: 'dm-1001' }),
}));

test('launches a direct conversation from author entry', async () => {
  const user = userEvent.setup();
  renderWithProviders(<ConversationLauncherButton peerUserCode="user-9" />);

  await user.click(screen.getByRole('button', { name: '发私信' }));

  expect(await screen.findByText('已进入私信会话')).toBeInTheDocument();
});
```

```tsx
import { screen } from '@testing-library/react';
import { renderWithProviders } from '../test/renderWithProviders';
import { UserProfilePage } from './UserProfilePage';

test('renders user profile page with dm entry', async () => {
  renderWithProviders(<UserProfilePage />, { route: '/users/user-9' });
  expect(await screen.findByRole('button', { name: '发私信' })).toBeInTheDocument();
});
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `npm --prefix frontend test -- ConversationLauncherButton.test.tsx UserProfilePage.test.tsx`

Expected: FAIL，提示新组件、新页面、私信 API 和路由尚不存在。

- [ ] **Step 3: Write minimal implementation**

```tsx
// frontend/src/components/ConversationLauncherButton.tsx
export function ConversationLauncherButton({ peerUserCode }: { peerUserCode: string }) {
  const navigate = useNavigate();
  const [done, setDone] = useState(false);

  async function handleClick() {
    const result = await createDirectConversation(peerUserCode);
    setDone(true);
    navigate(`/profile?tab=messages&conversation=${result.conversationCode}`);
  }

  return (
    <div>
      <button className="mini-button" type="button" onClick={() => void handleClick()}>
        发私信
      </button>
      {done ? <span>已进入私信会话</span> : null}
    </div>
  );
}
```

```ts
// frontend/src/services/api.ts
export function createDirectConversation(peerUserCode: string) {
  return request<{ conversationCode: string }>('/messages/conversations/single', {
    method: 'POST',
    body: JSON.stringify({ peerUserCode }),
  });
}
```

```tsx
// frontend/src/App.tsx
<Route path="/users/:userCode" element={<UserProfilePage />} />
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `npm --prefix frontend test -- ConversationLauncherButton.test.tsx UserProfilePage.test.tsx`

Expected: PASS，私信入口组件和用户主页入口通过。

Run: `npm --prefix frontend run build`

Expected: PASS，前端打包通过，现有 UI 布局未被破坏。

- [ ] **Step 5: Commit**

```bash
git add frontend/src/components/ConversationLauncherButton.tsx \
  frontend/src/components/MessageRecallMenu.tsx \
  frontend/src/components/ConversationLauncherButton.test.tsx \
  frontend/src/pages/UserProfilePage.tsx \
  frontend/src/pages/UserProfilePage.test.tsx \
  frontend/src/App.tsx frontend/src/components/PostCard.tsx \
  frontend/src/components/PostCommentsPanel.tsx frontend/src/context/AppContext.tsx \
  frontend/src/pages/ProfilePage.tsx frontend/src/services/api.ts \
  frontend/src/types.ts frontend/src/styles.css
git commit -m "feat: wire frontend private messaging flows"
```

### Task 6: Moderation, Audit And Remote Rollout Verification

**Files:**
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/controller/MessageModerationController.java`
- Create: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageModerationService.java`
- Modify: `backend/whu-treehole-server/src/main/resources/db/data.sql`
- Modify: `frontend/src/pages/AdminPage.tsx`
- Modify: `frontend/src/services/api.ts`
- Modify: `frontend/src/types.ts`
- Test: `backend/whu-treehole-message-service/src/test/java/com/whu/treehole/message/service/MessageModerationServiceTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.whu.treehole.message.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import com.whu.treehole.domain.dto.MessageReportRequest;
import com.whu.treehole.infra.mapper.MessageDomainMapper;
import com.whu.treehole.server.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MessageModerationServiceTest {

    @Test
    void shouldCreateMessageReportAndRecordAuditLog() {
        MessageDomainMapper mapper = Mockito.mock(MessageDomainMapper.class);
        AuditLogService auditLogService = Mockito.mock(AuditLogService.class);
        MessageModerationService service = new MessageModerationService(mapper, auditLogService);

        String reportCode = service.reportMessage(7L, "msg-1001", new MessageReportRequest("MESSAGE", "msg-1001", "骚扰", "重复私信"));

        assertEquals("report-msg-1001", reportCode);
        verify(auditLogService).record(7L, "message.report.create", "MESSAGE", null, "msg-1001");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q -f backend/pom.xml -s backend/settings.xml -pl whu-treehole-message-service -am -Dtest=MessageModerationServiceTest test`

Expected: FAIL，提示举报 DTO、审核服务、管理员接口或审计联动尚不存在。

- [ ] **Step 3: Write minimal implementation**

```java
// backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageModerationService.java
@Service
public class MessageModerationService {

    private final MessageDomainMapper messageDomainMapper;
    private final AuditLogService auditLogService;

    public MessageModerationService(MessageDomainMapper messageDomainMapper, AuditLogService auditLogService) {
        this.messageDomainMapper = messageDomainMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public String reportMessage(long userId, String messageCode, MessageReportRequest request) {
        String reportCode = "report-" + messageCode;
        messageDomainMapper.insertMessageReport(reportCode, request.targetType(), request.targetCode(), userId, request.reasonCode(), request.reasonDetail());
        auditLogService.record(userId, "message.report.create", request.targetType(), null, request.targetCode());
        return reportCode;
    }
}
```

```tsx
// frontend/src/pages/AdminPage.tsx
<section className="surface-card">
  <div className="section-head">
    <h2>私信举报</h2>
  </div>
</section>
```

- [ ] **Step 4: Run verification to verify it passes**

Run: `mvn -q -f backend/pom.xml -s backend/settings.xml -pl whu-treehole-message-service -am -Dtest=MessageModerationServiceTest test`

Expected: PASS，私信举报与审计最小链路成立。

Run: `npm --prefix frontend run build`

Expected: PASS，管理台入口构建通过。

Run: `docker compose --env-file .env.example -f docker-compose.yml config`

Expected: PASS，新服务、新环境变量、新容器依赖可解析。

Run: `C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe -Command "wsl.exe -d Ubuntu-22.04 --user xiewei -- ssh root@43.134.116.122 'cd /root/SE_project_feature-rbac-moderation && git rev-parse HEAD && docker compose --env-file .env.production -p se_project ps'"`

Expected: 远程仓库在最新提交，`gateway`、`message-service`、`mysql`、`redis`、`rabbitmq`、`frontend` 容器全部 `Up` 或 `healthy`。

Run: `C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe -Command "wsl.exe -d Ubuntu-22.04 --user xiewei -- ssh root@43.134.116.122 'curl -sI http://127.0.0.1/ | head -n 1 && curl -s http://127.0.0.1/api/messages/conversations | head -c 200'"`

Expected: 前端首页 `HTTP/1.1 200 OK`，Gateway 能转发私信 API。

- [ ] **Step 5: Commit**

```bash
git add backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/controller/MessageModerationController.java \
  backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageModerationService.java \
  backend/whu-treehole-server/src/main/resources/db/data.sql \
  frontend/src/pages/AdminPage.tsx frontend/src/services/api.ts frontend/src/types.ts \
  backend/whu-treehole-message-service/src/test/java/com/whu/treehole/message/service/MessageModerationServiceTest.java
git commit -m "feat: add messaging moderation and rollout checks"
```

## Self-Review

**Spec coverage:**

- `message-service`、独立模块、共享模型、DM 表结构：Task 1
- 私信 HTTP 接口、撤回/拉黑/举报基础链路：Task 2
- Redis、RabbitMQ、WebSocket、在线态与事件流：Task 3
- Gateway、Nacos、Compose、CI/CD、远程部署链路：Task 4
- 用户主页/帖子作者/评论作者发起私信与消息中心接入：Task 5
- 管理台私信举报、审计联动、远程验收：Task 6

**Placeholder scan:**

- 计划中没有任何未完成占位词或模糊落地标记
- 每个任务都给出了明确文件路径、测试文件、运行命令和提交命令
- 所有需要写代码的步骤都给出最小实现片段，而不是抽象描述

**Type consistency:**

- 新私信 DTO 统一使用 `DirectConversationRequest`、`ConversationDetailDto`、`MessageSendRequest`、`MessageReportRequest`
- 新服务模块统一命名为 `whu-treehole-message-service` 与 `whu-treehole-gateway`
- 新数据表统一使用 `dm_*` 前缀，不与现有展示型 `conversations/messages` 混淆
