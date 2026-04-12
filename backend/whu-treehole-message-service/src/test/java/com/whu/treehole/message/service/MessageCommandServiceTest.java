package com.whu.treehole.message.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.whu.treehole.domain.dto.MessageDto;
import com.whu.treehole.domain.dto.MessageSendRequest;
import com.whu.treehole.domain.enums.MessageStatus;
import com.whu.treehole.infra.mapper.MessageDomainMapper;
import com.whu.treehole.infra.model.DmConversationParticipantData;
import com.whu.treehole.infra.model.DmMessageData;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageCommandServiceTest {

    @Mock
    private MessageDomainMapper messageDomainMapper;

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

        Clock fixedClock = Clock.fixed(Instant.parse("2026-04-12T13:45:00Z"), ZoneId.of("Asia/Shanghai"));
        MessageCommandService service = new MessageCommandService(messageDomainMapper, fixedClock);

        service.sendMessage(7L, "dm-1001", new MessageSendRequest("msg-client-1", "  hello  "));

        ArgumentCaptor<DmMessageData> messageCaptor = ArgumentCaptor.forClass(DmMessageData.class);
        verify(messageDomainMapper).insertMessage(messageCaptor.capture());
        verify(messageDomainMapper).updateConversationAfterSend(18L, 201L, messageCaptor.getValue().getSentAt());
        verify(messageDomainMapper).increaseUnreadForPeer(18L, 7L);

        assertEquals("msg-client-1", messageCaptor.getValue().getClientMessageId());
        assertEquals("hello", messageCaptor.getValue().getContentPayload());
        assertEquals(MessageStatus.SENT, messageCaptor.getValue().getStatus());
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
