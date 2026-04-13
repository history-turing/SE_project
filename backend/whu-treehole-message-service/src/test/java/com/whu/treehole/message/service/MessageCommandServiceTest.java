package com.whu.treehole.message.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.whu.treehole.domain.dto.ConversationListItemDto;
import com.whu.treehole.domain.dto.MessageDto;
import com.whu.treehole.domain.dto.MessageRealtimeEventDto;
import com.whu.treehole.domain.dto.MessageRecipientStateDto;
import com.whu.treehole.domain.dto.MessageSendRequest;
import com.whu.treehole.domain.dto.UnreadNotificationDto;
import com.whu.treehole.domain.enums.MessageStatus;
import com.whu.treehole.infra.mapper.MessageDomainMapper;
import com.whu.treehole.infra.model.DmConversationParticipantData;
import com.whu.treehole.infra.model.DmMessageData;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageCommandServiceTest {

    @Mock
    private MessageDomainMapper messageDomainMapper;

    @Mock
    private MessageEventPublisher messageEventPublisher;

    @Mock
    private ConversationQueryService conversationQueryService;

    @Test
    void shouldPersistMessageAndIncreasePeerUnread() {
        when(messageDomainMapper.selectConversationIdByCode("dm-1001")).thenReturn(18L);
        when(messageDomainMapper.selectConversationParticipant(7L, "dm-1001"))
                .thenReturn(new DmConversationParticipantData());
        doAnswer(invocation -> {
            DmMessageData data = invocation.getArgument(0);
            data.setId(201L);
            return null;
        }).when(messageDomainMapper).insertMessage(any(DmMessageData.class));
        when(conversationQueryService.buildRecipientStates(any(), any(DmMessageData.class)))
                .thenReturn(List.of(
                        new MessageRecipientStateDto(
                                7L,
                                new ConversationListItemDto("dm-1001", "DIRECT", "peer", "tagline", "/avatar.png", "hello", "21:45", 0),
                                new MessageDto("msg-201", "me", "hello", "21:45", "TEXT", "SENT", false, null, true),
                                new UnreadNotificationDto(7L, 0, 0, 0, 0)),
                        new MessageRecipientStateDto(
                                9L,
                                new ConversationListItemDto("dm-1001", "DIRECT", "sender", "tagline", "/avatar.png", "hello", "21:45", 1),
                                new MessageDto("msg-201", "them", "hello", "21:45", "TEXT", "SENT", false, null, false),
                                new UnreadNotificationDto(9L, 1, 0, 0, 1))));

        Clock fixedClock = Clock.fixed(Instant.parse("2026-04-12T13:45:00Z"), ZoneId.of("Asia/Shanghai"));
        MessageCommandService service = new MessageCommandService(
                messageDomainMapper,
                fixedClock,
                messageEventPublisher,
                conversationQueryService,
                java.time.Duration.ofMinutes(2));

        service.sendMessage(7L, "dm-1001", new MessageSendRequest("msg-client-1", "  hello  "));

        ArgumentCaptor<DmMessageData> messageCaptor = ArgumentCaptor.forClass(DmMessageData.class);
        verify(messageDomainMapper).insertMessage(messageCaptor.capture());
        verify(messageDomainMapper).updateConversationAfterSend(18L, 201L, messageCaptor.getValue().getSentAt());
        verify(messageDomainMapper).increaseUnreadForPeer(18L, 7L);

        assertEquals("msg-client-1", messageCaptor.getValue().getClientMessageId());
        assertEquals("hello", messageCaptor.getValue().getContentPayload());
        assertEquals(MessageStatus.SENT, messageCaptor.getValue().getStatus());
        verify(messageEventPublisher).publish(argThat((MessageRealtimeEventDto event) ->
                "message.created".equals(event.type())
                        && "dm-1001".equals(event.conversationCode())
                        && event.recipientStates() != null
                        && event.recipientStates().size() == 2
                        && event.recipientStates().stream().anyMatch(state ->
                                Long.valueOf(9L).equals(state.userId())
                                        && state.message() != null
                                        && state.conversation() != null
                                        && state.unreadNotification() != null
                                        && Integer.valueOf(1).equals(state.unreadNotification().messagesUnread()))));
    }

    @Test
    void shouldRecallOwnMessageAndReturnRecalledPlaceholder() {
        DmConversationParticipantData participantData = new DmConversationParticipantData();
        participantData.setConversationId(18L);
        when(messageDomainMapper.selectConversationParticipant(7L, "dm-1001")).thenReturn(participantData);

        DmMessageData messageData = new DmMessageData();
        messageData.setId(201L);
        messageData.setMessageCode("msg-201");
        messageData.setConversationId(18L);
        messageData.setSenderUserId(7L);
        messageData.setStatus(MessageStatus.SENT);
        messageData.setMessageType(com.whu.treehole.domain.enums.MessageType.TEXT);
        messageData.setContentPayload("hello");
        messageData.setSentAt(LocalDateTime.of(2026, 4, 12, 21, 44));
        when(messageDomainMapper.selectMessageByCode(7L, "dm-1001", "msg-201")).thenReturn(messageData);
        Clock fixedClock = Clock.fixed(Instant.parse("2026-04-12T13:45:00Z"), ZoneId.of("Asia/Shanghai"));
        MessageCommandService service = new MessageCommandService(messageDomainMapper, fixedClock);

        MessageDto recalled = service.recallMessage(7L, "dm-1001", "msg-201");

        verify(messageDomainMapper).recallMessage(201L, MessageStatus.REVOKED, messageData.getRecalledAt());
        assertEquals("msg-201", recalled.id());
        assertEquals("me", recalled.sender());
        assertEquals("你撤回了一条消息", recalled.text());
        assertEquals("REVOKED", recalled.status());
        assertEquals("TEXT", recalled.messageType());
        assertFalse(recalled.canRecall());
        assertEquals(messageData.getRecalledAt().toString(), recalled.recalledAt());
    }
}
