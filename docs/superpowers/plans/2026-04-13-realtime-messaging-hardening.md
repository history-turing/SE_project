# Realtime Messaging Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build contract-consistent realtime direct messaging with unread badge updates, remove runtime hardcoded demo data, and preserve anonymity when users start a conversation from an anonymous post.

**Architecture:** Keep message writes on HTTP and push changes down over WebSocket through the existing gateway and `message-service`. Unify backend DTOs, mapper output, frontend ViewModels, and final UI state so the same business data is represented exactly once per layer. Introduce an explicit anonymous-post conversation path so anonymity is enforced by the backend instead of being “hidden” only in the UI.

**Tech Stack:** Spring Boot 3, MyBatis XML mappers, RabbitMQ, Redis, WebSocket, React, TypeScript, Vite, Vitest, Maven

---

## File Structure

### Backend

- Modify: `backend/whu-treehole-server/src/main/resources/db/schema.sql`
- Modify: `backend/whu-treehole-server/src/main/resources/db/data.sql`
- Modify: `backend/whu-treehole-server/src/main/java/com/whu/treehole/server/config/WebCorsProperties.java`
- Modify: `backend/whu-treehole-server/src/main/java/com/whu/treehole/server/config/WebConfig.java`
- Modify: `backend/whu-treehole-server/src/test/java/com/whu/treehole/server/config/WebConfigCorsTest.java`
- Modify: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/DirectConversationRequest.java`
- Modify: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/ConversationListItemDto.java`
- Modify: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/ConversationDetailDto.java`
- Modify: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/ConversationPeerDto.java`
- Modify: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/MessageEventDto.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/UnreadNotificationDto.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/MessageRealtimeEventDto.java`
- Modify: `backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/mapper/MessageDomainMapper.java`
- Modify: `backend/whu-treehole-infra/src/main/resources/mapper/MessageDomainMapper.xml`
- Create: `backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/model/DmUnreadAggregateData.java`
- Modify: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/controller/MessageConversationController.java`
- Modify: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/ConversationCommandService.java`
- Modify: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/ConversationQueryService.java`
- Modify: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageCommandService.java`
- Modify: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageEventPublisher.java`
- Modify: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageEventConsumer.java`
- Modify: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageSessionRegistry.java`
- Test: `backend/whu-treehole-message-service/src/test/java/com/whu/treehole/message/service/ConversationCommandServiceTest.java`
- Test: `backend/whu-treehole-message-service/src/test/java/com/whu/treehole/message/service/ConversationQueryServiceTest.java`
- Test: `backend/whu-treehole-message-service/src/test/java/com/whu/treehole/message/service/MessageCommandServiceTest.java`

### Frontend

- Modify: `frontend/src/types.ts`
- Modify: `frontend/src/services/api.ts`
- Create: `frontend/src/realtime/messageRealtimeClient.ts`
- Create: `frontend/src/realtime/messageRealtimeClient.test.ts`
- Create: `frontend/src/mappers/messageViewModels.ts`
- Modify: `frontend/src/context/AuthContext.tsx`
- Modify: `frontend/src/context/AppContext.tsx`
- Modify: `frontend/src/components/AppShell.tsx`
- Modify: `frontend/src/components/ConversationLauncherButton.tsx`
- Modify: `frontend/src/components/PostCard.tsx`
- Modify: `frontend/src/components/PostCommentsPanel.tsx`
- Modify: `frontend/src/pages/ProfilePage.tsx`
- Modify: `frontend/src/pages/UserProfilePage.tsx`
- Test: `frontend/src/components/ConversationLauncherButton.test.tsx`
- Test: `frontend/src/context/AppContext.test.tsx`
- Test: `frontend/src/pages/ProfilePage.test.tsx`

### Verification and Docs

- Modify: `backend/README.md`
- Modify: `docs/ops/remote-deploy-baseline.md`

---

### Task 1: Harden The Backend Conversation Contract

**Files:**
- Modify: `backend/whu-treehole-server/src/main/resources/db/schema.sql`
- Modify: `backend/whu-treehole-server/src/main/resources/db/data.sql`
- Modify: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/DirectConversationRequest.java`
- Modify: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/ConversationListItemDto.java`
- Modify: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/ConversationDetailDto.java`
- Modify: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/ConversationPeerDto.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/UnreadNotificationDto.java`
- Create: `backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/MessageRealtimeEventDto.java`
- Modify: `backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/mapper/MessageDomainMapper.java`
- Modify: `backend/whu-treehole-infra/src/main/resources/mapper/MessageDomainMapper.xml`
- Create: `backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/model/DmUnreadAggregateData.java`
- Test: `backend/whu-treehole-message-service/src/test/java/com/whu/treehole/message/service/ConversationCommandServiceTest.java`

- [ ] **Step 1: Write failing tests for anonymous-conversation creation and contract shape**

```java
@Test
void shouldCreateAnonymousPostConversationWithoutLeakingPeerIdentity() {
    DirectConversationRequest request = new DirectConversationRequest("user-9", "post-1001", true);

    String conversationCode = conversationCommandService.createOrGetSingleConversation(7L, request, 9L);

    ConversationDetailDto detail = conversationQueryService.getConversationDetail(7L, conversationCode);
    assertEquals("ANONYMOUS_POST", detail.conversationType());
    assertEquals("匿名树洞作者", detail.peer().name());
    assertNull(detail.peer().userCode());
}
```

- [ ] **Step 2: Run the message-service tests to confirm they fail**

Run: `mvn --% -q -f backend/pom.xml -s backend/settings.xml -pl whu-treehole-message-service -am test -Dtest=ConversationCommandServiceTest,ConversationQueryServiceTest -Dsurefire.failIfNoSpecifiedTests=false`

Expected: FAIL because the request/DTO/mapper/schema do not yet support anonymous-post conversations or the extra contract fields.

- [ ] **Step 3: Extend the schema, request DTO, and mapper contract**

```sql
ALTER TABLE dm_conversations
    ADD COLUMN conversation_scene VARCHAR(32) NOT NULL DEFAULT 'DIRECT',
    ADD COLUMN source_post_code VARCHAR(64) NULL,
    ADD COLUMN anonymous_flag TINYINT(1) NOT NULL DEFAULT 0;
```

```java
public record DirectConversationRequest(
        @NotBlank String peerUserCode,
        String sourcePostCode,
        boolean anonymousEntry
) {
}
```

```java
public record ConversationListItemDto(
        String conversationCode,
        String conversationType,
        String peerName,
        String peerSubtitle,
        String peerAvatarUrl,
        String lastMessage,
        String displayTime,
        Integer unreadCount
) {
}
```

- [ ] **Step 4: Implement anonymous-scene creation and read-side anonymization**

```java
boolean anonymousScene = request.anonymousEntry() && request.sourcePostCode() != null && !request.sourcePostCode().isBlank();
conversationData.setConversationType(anonymousScene ? "ANONYMOUS_POST" : "DIRECT");
conversationData.setConversationScene(anonymousScene ? "ANONYMOUS_POST" : "DIRECT");
conversationData.setSourcePostCode(anonymousScene ? request.sourcePostCode() : null);
conversationData.setAnonymousFlag(anonymousScene);
```

```java
private ConversationPeerDto toPeerDto(UserProfileData data, DmConversationData conversationData) {
    if (conversationData.getAnonymousFlag()) {
        return new ConversationPeerDto(null, "匿名树洞作者", "匿名私信会话", "/images/avatar-anonymous.svg");
    }
    return new ConversationPeerDto(data.getUserCode(), data.getName(), data.getTagline(), data.getAvatarUrl());
}
```

- [ ] **Step 5: Re-run the focused message-service tests**

Run: `mvn --% -q -f backend/pom.xml -s backend/settings.xml -pl whu-treehole-message-service -am test -Dtest=ConversationCommandServiceTest,ConversationQueryServiceTest -Dsurefire.failIfNoSpecifiedTests=false`

Expected: PASS, proving anonymous-post conversations are represented as a first-class backend contract.

- [ ] **Step 6: Commit**

```bash
git add backend/whu-treehole-server/src/main/resources/db/schema.sql backend/whu-treehole-server/src/main/resources/db/data.sql backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/DirectConversationRequest.java backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/ConversationListItemDto.java backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/ConversationDetailDto.java backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/ConversationPeerDto.java backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/UnreadNotificationDto.java backend/whu-treehole-domain/src/main/java/com/whu/treehole/domain/dto/MessageRealtimeEventDto.java backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/mapper/MessageDomainMapper.java backend/whu-treehole-infra/src/main/resources/mapper/MessageDomainMapper.xml backend/whu-treehole-infra/src/main/java/com/whu/treehole/infra/model/DmUnreadAggregateData.java backend/whu-treehole-message-service/src/test/java/com/whu/treehole/message/service/ConversationCommandServiceTest.java
git commit -m "feat: add anonymous-safe dm contract"
```

### Task 2: Publish Rich Realtime Events And Unread Aggregates

**Files:**
- Modify: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/ConversationQueryService.java`
- Modify: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/ConversationCommandService.java`
- Modify: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageCommandService.java`
- Modify: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageEventPublisher.java`
- Modify: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageEventConsumer.java`
- Modify: `backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageSessionRegistry.java`
- Test: `backend/whu-treehole-message-service/src/test/java/com/whu/treehole/message/service/MessageCommandServiceTest.java`

- [ ] **Step 1: Write failing tests for event payload completeness and unread aggregate updates**

```java
@Test
void shouldPublishRealtimeEventWithConversationSummaryMessageSummaryAndUnreadState() {
    MessageDto dto = messageCommandService.sendMessage(7L, "dm-1001", new MessageSendRequest("web-1", "hello"));

    verify(messageEventPublisher).publish(argThat(event ->
            "message.created".equals(event.type())
                    && event.conversation() != null
                    && event.message() != null
                    && event.recipientStates() != null
                    && !event.recipientStates().isEmpty()
    ));
}
```

- [ ] **Step 2: Run the focused realtime tests and verify they fail**

Run: `mvn --% -q -f backend/pom.xml -s backend/settings.xml -pl whu-treehole-message-service -am test -Dtest=MessageCommandServiceTest -Dsurefire.failIfNoSpecifiedTests=false`

Expected: FAIL because current events only carry `type/conversationCode/messageCode/targetUserIds`.

- [ ] **Step 3: Extend the backend read services to build reusable summaries**

```java
public ConversationListItemDto getConversationSummary(long userId, String conversationCode) {
    ConversationData data = messageDomainMapper.selectConversationSummary(userId, conversationCode);
    return toConversationListItem(data);
}

public UnreadNotificationDto getUnreadNotification(long userId) {
    DmUnreadAggregateData aggregate = messageDomainMapper.selectUnreadAggregate(userId);
    return new UnreadNotificationDto(
            aggregate == null ? 0 : aggregate.getMessagesUnread(),
            0,
            0
    );
}
```

- [ ] **Step 4: Publish rich events after send, recall, and read**

```java
messageEventPublisher.publish(new MessageRealtimeEventDto(
        "message.created",
        conversationQueryService.getConversationSummaryForParticipants(conversationCode),
        toMessageDto(operatorUserId, messageData),
        conversationQueryService.getRecipientUnreadStates(conversationCode)
));
```

```java
messageEventPublisher.publish(new MessageRealtimeEventDto(
        "conversation.read",
        conversationQueryService.getConversationSummary(userId, conversationCode),
        null,
        List.of(conversationQueryService.getRecipientUnreadState(userId, conversationCode))
));
```

- [ ] **Step 5: Re-run the focused realtime tests**

Run: `mvn --% -q -f backend/pom.xml -s backend/settings.xml -pl whu-treehole-message-service -am test -Dtest=MessageCommandServiceTest,ConversationQueryServiceTest -Dsurefire.failIfNoSpecifiedTests=false`

Expected: PASS, showing that events now contain enough data for the frontend to update without extra ad hoc fetches.

- [ ] **Step 6: Commit**

```bash
git add backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/ConversationQueryService.java backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/ConversationCommandService.java backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageCommandService.java backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageEventPublisher.java backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageEventConsumer.java backend/whu-treehole-message-service/src/main/java/com/whu/treehole/message/service/MessageSessionRegistry.java backend/whu-treehole-message-service/src/test/java/com/whu/treehole/message/service/MessageCommandServiceTest.java
git commit -m "feat: publish rich dm realtime events"
```

### Task 3: Remove Frontend Runtime Demo State And Unify View Models

**Files:**
- Modify: `frontend/src/types.ts`
- Modify: `frontend/src/services/api.ts`
- Create: `frontend/src/mappers/messageViewModels.ts`
- Modify: `frontend/src/context/AppContext.tsx`
- Test: `frontend/src/context/AppContext.test.tsx`

- [ ] **Step 1: Write failing frontend tests for empty initial runtime state and unified message mapping**

```tsx
test('boots with empty live conversations until api resolves', () => {
  renderWithProviders(<AppProvider><div>ok</div></AppProvider>);
  expect(screen.queryByText('珞珈山下的小狐狸')).not.toBeInTheDocument();
});
```

```tsx
test('maps anonymous conversation dto to anonymous-safe view model', () => {
  const viewModel = mapConversationDetail({
    conversationCode: 'dm-1001',
    conversationType: 'ANONYMOUS_POST',
    peer: { userCode: null, name: '匿名树洞作者', subtitle: '匿名私信会话', avatarUrl: '/images/avatar-anonymous.svg' },
    lastMessage: '你好',
    lastMessageTime: '10:30',
    unreadCount: 1,
    messages: []
  });
  expect(viewModel.peer.userCode).toBe('');
  expect(viewModel.peer.name).toBe('匿名树洞作者');
});
```

- [ ] **Step 2: Run the frontend tests and verify they fail**

Run: `npm --prefix frontend test -- --run AppContext.test.tsx`

Expected: FAIL because `AppContext` still starts from `initialConversations` and `siteData.ts`.

- [ ] **Step 3: Introduce unified DTO -> ViewModel mapping and remove demo-message bootstrapping**

```ts
export function mapConversationSummary(dto: DmConversationListItemResponse): DmConversationSummary {
  return {
    conversationCode: dto.conversationCode,
    conversationType: dto.conversationType,
    peer: {
      userCode: dto.peer?.userCode ?? '',
      name: dto.peer?.name ?? '私信会话',
      subtitle: dto.peer?.subtitle ?? '',
      avatar: dto.peer?.avatarUrl ?? '',
    },
    lastMessage: dto.lastMessage,
    displayTime: dto.displayTime,
    unreadCount: dto.unreadCount ?? 0,
  };
}
```

```ts
const [conversations, setConversations] = useState<DmConversationSummary[]>([]);
const [activeConversationCode, setActiveConversationCode] = useState('');
const [activeConversation, setActiveConversation] = useState<DmConversationDetail | null>(null);
```

- [ ] **Step 4: Re-run the focused frontend tests**

Run: `npm --prefix frontend test -- --run AppContext.test.tsx`

Expected: PASS, proving that runtime DM state no longer depends on static demo conversation data.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/types.ts frontend/src/services/api.ts frontend/src/mappers/messageViewModels.ts frontend/src/context/AppContext.tsx frontend/src/context/AppContext.test.tsx
git commit -m "refactor: unify dm view models"
```

### Task 4: Add Frontend Realtime Client And Unread Badge State

**Files:**
- Create: `frontend/src/realtime/messageRealtimeClient.ts`
- Create: `frontend/src/realtime/messageRealtimeClient.test.ts`
- Modify: `frontend/src/context/AuthContext.tsx`
- Modify: `frontend/src/context/AppContext.tsx`
- Modify: `frontend/src/components/AppShell.tsx`
- Modify: `frontend/src/pages/ProfilePage.tsx`
- Test: `frontend/src/pages/ProfilePage.test.tsx`

- [ ] **Step 1: Write failing tests for realtime event application and topbar unread badge updates**

```tsx
test('applies message.created to inactive conversation and shows unread badge in shell', async () => {
  // simulate client event dispatch
  realtimeBus.emit({
    type: 'message.created',
    conversation: { conversationCode: 'dm-1002', unreadCount: 2, lastMessage: '新消息', displayTime: '12:30' },
    message: null,
    recipientStates: [{ userId: 7, messagesUnread: 2, totalUnread: 2 }],
  });

  expect(await screen.findByText('2')).toBeInTheDocument();
});
```

- [ ] **Step 2: Run the frontend realtime tests and verify they fail**

Run: `npm --prefix frontend test -- --run ProfilePage.test.tsx`

Expected: FAIL because there is no WebSocket client, no unread aggregate state, and the bell button is static.

- [ ] **Step 3: Build the realtime client and wire it into auth/app state**

```ts
export function createMessageRealtimeClient({ token, onEvent, onStateChange }: MessageRealtimeClientOptions) {
  let socket: WebSocket | null = null;
  let retryTimer: number | null = null;

  function connect() {
    socket = new WebSocket(resolveRealtimeUrl(token));
    socket.onmessage = (event) => onEvent(JSON.parse(event.data) as MessageRealtimeEvent);
    socket.onclose = () => scheduleReconnect();
  }

  return { connect, disconnect };
}
```

```ts
const [notificationSummary, setNotificationSummary] = useState<NotificationSummary>({
  messagesUnread: 0,
  interactionsUnread: 0,
  systemUnread: 0,
  totalUnread: 0,
  hasUnread: false,
});
```

- [ ] **Step 4: Apply events to conversation state and bell badge**

```ts
if (event.type === 'message.created' && event.conversation) {
  setConversations((current) => mergeConversationSummary(current, event.conversation));
  setNotificationSummary(toNotificationSummary(event.recipientStates, currentUserId));
}

if (event.type === 'message.recalled' && event.message && event.conversation?.conversationCode === activeConversationCode) {
  setActiveConversation((current) => applyRecalledMessage(current, event.message));
}
```

```tsx
<button className="ghost-button" type="button" aria-label="通知" onClick={() => navigate('/profile?tab=messages')}>
  <Icon name="bell" className="icon" />
  {notificationSummary.hasUnread ? <span className="topbar-badge">{notificationSummary.totalUnread}</span> : null}
</button>
```

- [ ] **Step 5: Re-run the focused frontend realtime tests**

Run: `npm --prefix frontend test -- --run messageRealtimeClient.test.ts ProfilePage.test.tsx AppContext.test.tsx`

Expected: PASS, showing that realtime events now mutate the same state the UI renders.

- [ ] **Step 6: Commit**

```bash
git add frontend/src/realtime/messageRealtimeClient.ts frontend/src/realtime/messageRealtimeClient.test.ts frontend/src/context/AuthContext.tsx frontend/src/context/AppContext.tsx frontend/src/components/AppShell.tsx frontend/src/pages/ProfilePage.tsx frontend/src/pages/ProfilePage.test.tsx
git commit -m "feat: add realtime dm updates and unread badge"
```

### Task 5: Fix Anonymous Messaging Entry Points And Page-Level Identity Leaks

**Files:**
- Modify: `frontend/src/components/ConversationLauncherButton.tsx`
- Modify: `frontend/src/components/PostCard.tsx`
- Modify: `frontend/src/components/PostCommentsPanel.tsx`
- Modify: `frontend/src/pages/UserProfilePage.tsx`
- Modify: `frontend/src/services/api.ts`
- Test: `frontend/src/components/ConversationLauncherButton.test.tsx`
- Test: `frontend/src/components/PostCard.test.tsx`

- [ ] **Step 1: Write failing tests for anonymous-post launcher behavior**

```tsx
test('launches anonymous conversation request from anonymous post without linking to real profile', async () => {
  renderWithProviders(<ConversationLauncherButton peerUserCode="user-9" sourcePostCode="post-1001" anonymousEntry />);
  await userEvent.click(screen.getByRole('button', { name: '发私信' }));
  expect(createDirectConversation).toHaveBeenCalledWith({
    peerUserCode: 'user-9',
    sourcePostCode: 'post-1001',
    anonymousEntry: true,
  });
});
```

```tsx
test('does not render real-profile author link for anonymous post', () => {
  renderWithProviders(<PostCard post={{ ...basePost, anonymous: true, authorUserCode: null }} />);
  expect(screen.queryByRole('link')).not.toBeInTheDocument();
});
```

- [ ] **Step 2: Run the focused launcher tests and verify they fail**

Run: `npm --prefix frontend test -- --run ConversationLauncherButton.test.tsx PostCard.test.tsx`

Expected: FAIL because the current launcher only accepts a plain `peerUserCode` and the author block still treats any `authorUserCode` as navigable.

- [ ] **Step 3: Extend the launcher API and gate profile links by anonymity**

```ts
export function createDirectConversation(payload: {
  peerUserCode: string;
  sourcePostCode?: string;
  anonymousEntry?: boolean;
}) {
  return request<string>('/dm/conversations/direct', {
    method: 'POST',
    body: JSON.stringify(payload),
  }).then((conversationCode) => ({ conversationCode }));
}
```

```tsx
export function ConversationLauncherButton({ peerUserCode, sourcePostCode, anonymousEntry, className = 'mini-button' }: Props) {
  const result = await createDirectConversation({ peerUserCode, sourcePostCode, anonymousEntry });
}
```

```tsx
if (post.anonymous || !post.authorUserCode) {
  return content;
}
```

- [ ] **Step 4: Re-run the focused anonymity tests**

Run: `npm --prefix frontend test -- --run ConversationLauncherButton.test.tsx PostCard.test.tsx`

Expected: PASS, proving the anonymous post entry path no longer routes users into a real-profile flow.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/components/ConversationLauncherButton.tsx frontend/src/components/PostCard.tsx frontend/src/components/PostCommentsPanel.tsx frontend/src/pages/UserProfilePage.tsx frontend/src/services/api.ts frontend/src/components/ConversationLauncherButton.test.tsx frontend/src/components/PostCard.test.tsx
git commit -m "fix: preserve anonymity in dm entry points"
```

### Task 6: Verify CORS Config Cleanup, Full Test Coverage, And Remote Deployment

**Files:**
- Modify: `backend/whu-treehole-server/src/main/java/com/whu/treehole/server/config/WebCorsProperties.java`
- Modify: `backend/whu-treehole-server/src/main/java/com/whu/treehole/server/config/WebConfig.java`
- Modify: `backend/whu-treehole-server/src/test/java/com/whu/treehole/server/config/WebConfigCorsTest.java`
- Modify: `backend/README.md`
- Modify: `docs/ops/remote-deploy-baseline.md`

- [ ] **Step 1: Write the failing CORS/property test**

```java
@Test
void shouldReadAllowedOriginsFromPropertiesInsteadOfHardcodingProductionHost() {
    WebCorsProperties properties = new WebCorsProperties();
    properties.setAllowedOrigins(List.of("https://treehole.example.com"));

    WebConfig webConfig = new WebConfig(mock(AuthInterceptor.class), properties);
    CorsConfiguration configuration = extractConfiguration(webConfig);

    assertTrue(configuration.getAllowedOrigins().contains("https://treehole.example.com"));
}
```

- [ ] **Step 2: Run the focused backend config test and verify it fails**

Run: `mvn --% -q -f backend/pom.xml -s backend/settings.xml -pl whu-treehole-server -am test -Dtest=WebConfigCorsTest -Dsurefire.failIfNoSpecifiedTests=false`

Expected: FAIL if the configuration still assumes hardcoded production hosts or the test setup no longer matches.

- [ ] **Step 3: Implement the property-driven CORS cleanup and update docs**

```java
private List<String> allowedOrigins = new ArrayList<>(List.of(
        "http://localhost:5173",
        "http://127.0.0.1:5173",
        "http://localhost:4173"
));
```

```md
- Set `TREEHOLE_WEB_CORS_ALLOWED_ORIGINS=https://your-domain.example,http://43.134.116.122`
- Do not hardcode production origins in Java source files.
```

- [ ] **Step 4: Run the full local verification suite**

Run: `mvn --% -q -f backend/pom.xml -s backend/settings.xml test`

Expected: PASS

Run: `npm --prefix frontend test -- --run`

Expected: PASS

Run: `npm --prefix frontend run build`

Expected: PASS

- [ ] **Step 5: Deploy and verify on the remote Docker environment**

Run: `git push origin feature-rbac-moderation`

Expected: PASS and branch updated on GitHub.

Run: `wsl.exe -d Ubuntu-22.04 --user xiewei -- ssh root@43.134.116.122 "cd /root/SE_project_feature-rbac-moderation && git pull --ff-only origin feature-rbac-moderation && bash ./scripts/deploy.sh"`

Expected: PASS and all containers healthy.

Run: `wsl.exe -d Ubuntu-22.04 --user xiewei -- ssh root@43.134.116.122 "docker compose --env-file /root/SE_project_feature-rbac-moderation/.env.production -p se_project ps"`

Expected: `gateway` / `backend` / `message-service` / `redis` / `rabbitmq` all show `Up`.

Run: `wsl.exe -d Ubuntu-22.04 --user xiewei -- ssh root@43.134.116.122 "docker logs --tail 200 se_project_message-service_1"`

Expected: no startup exception; WebSocket, RabbitMQ, Redis startup normal.

- [ ] **Step 6: Perform browser-level remote acceptance**

```text
1. Use two real accounts on the remote site.
2. Start a normal DM from a non-anonymous profile and confirm real identity is shown.
3. Start a DM from an anonymous post and confirm real identity is not shown anywhere.
4. Send a message from account A to account B and confirm B sees it without refresh.
5. Confirm account B sees the topbar red badge before opening the conversation.
6. Open the conversation and confirm the badge count drops immediately.
7. Recall a message and confirm both sides update without refresh.
```

- [ ] **Step 7: Commit**

```bash
git add backend/whu-treehole-server/src/main/java/com/whu/treehole/server/config/WebCorsProperties.java backend/whu-treehole-server/src/main/java/com/whu/treehole/server/config/WebConfig.java backend/whu-treehole-server/src/test/java/com/whu/treehole/server/config/WebConfigCorsTest.java backend/README.md docs/ops/remote-deploy-baseline.md
git commit -m "chore: finalize realtime messaging hardening"
```

---

## Self-Review

### Spec coverage

- Runtime hardcoding cleanup is covered by Tasks 3 and 6.
- Realtime DM updates and unread red badge are covered by Tasks 2 and 4.
- Frontend/backend/UI consistency is covered by Tasks 1, 3, 4, and 6.
- Anonymous-post DM safety is covered by Tasks 1 and 5.

### Placeholder scan

- No `TODO`/`TBD` placeholders remain.
- Each task names concrete files, commands, and test expectations.

### Type consistency

- The plan uses `conversationType`, `MessageRealtimeEventDto`, and `UnreadNotificationDto` consistently across backend and frontend tasks.
- Anonymous conversation flow is always expressed through `sourcePostCode + anonymousEntry`.

