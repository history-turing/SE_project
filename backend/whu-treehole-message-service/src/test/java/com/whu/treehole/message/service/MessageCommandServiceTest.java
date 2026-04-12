package com.whu.treehole.message.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.whu.treehole.domain.dto.MessageSendRequest;
import com.whu.treehole.domain.enums.MessageStatus;
import com.whu.treehole.infra.mapper.MessageDomainMapper;
import com.whu.treehole.infra.model.DmConversationParticipantData;
import com.whu.treehole.infra.model.DmMessageData;
import java.time.Clock;
import java.time.Instant;
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

        service.sendMessage(7L, "dm-1001", new MessageSendRequest("msg-client-1", "  晚上好  "));

        ArgumentCaptor<DmMessageData> messageCaptor = ArgumentCaptor.forClass(DmMessageData.class);
        verify(messageDomainMapper).insertMessage(messageCaptor.capture());
        verify(messageDomainMapper).updateConversationAfterSend(18L, 201L, messageCaptor.getValue().getSentAt());
        verify(messageDomainMapper).increaseUnreadForPeer(18L, 7L);

        assertEquals("msg-client-1", messageCaptor.getValue().getClientMessageId());
        assertEquals("晚上好", messageCaptor.getValue().getContentPayload());
        assertEquals(MessageStatus.SENT, messageCaptor.getValue().getStatus());
    }
}
