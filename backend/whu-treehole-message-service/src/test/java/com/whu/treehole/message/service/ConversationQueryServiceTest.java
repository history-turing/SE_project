package com.whu.treehole.message.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.whu.treehole.domain.dto.ConversationDetailDto;
import com.whu.treehole.domain.enums.ConversationStatus;
import com.whu.treehole.domain.enums.MessageStatus;
import com.whu.treehole.domain.enums.MessageType;
import com.whu.treehole.infra.mapper.MessageDomainMapper;
import com.whu.treehole.infra.model.DmConversationData;
import com.whu.treehole.infra.model.DmConversationParticipantData;
import com.whu.treehole.infra.model.DmMessageData;
import com.whu.treehole.infra.model.UserProfileData;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConversationQueryServiceTest {

    @Mock
    private MessageDomainMapper messageDomainMapper;

    @Test
    void shouldHideRealPeerIdentityForAnonymousConversation() {
        DmConversationData conversationData = new DmConversationData();
        conversationData.setConversationCode("dm-1001");
        conversationData.setConversationType("ANONYMOUS_POST");
        conversationData.setConversationScene("ANONYMOUS_POST");
        conversationData.setAnonymousFlag(Boolean.TRUE);
        conversationData.setStatus(ConversationStatus.ACTIVE);
        when(messageDomainMapper.selectConversationByCodeAndUserId(7L, "dm-1001")).thenReturn(conversationData);

        DmConversationParticipantData participantData = new DmConversationParticipantData();
        participantData.setUnreadCount(1);
        when(messageDomainMapper.selectConversationParticipant(7L, "dm-1001")).thenReturn(participantData);

        UserProfileData realPeerData = new UserProfileData();
        realPeerData.setUserCode("user-9");
        realPeerData.setName("真实姓名");
        realPeerData.setTagline("真实签名");
        realPeerData.setAvatarUrl("https://example.com/real-avatar.png");
        when(messageDomainMapper.selectConversationPeer(7L, "dm-1001")).thenReturn(realPeerData);

        DmMessageData messageData = new DmMessageData();
        messageData.setMessageCode("msg-201");
        messageData.setSenderUserId(9L);
        messageData.setStatus(MessageStatus.SENT);
        messageData.setMessageType(MessageType.TEXT);
        messageData.setContentPayload("hello");
        messageData.setSentAt(LocalDateTime.of(2026, 4, 13, 10, 30));
        when(messageDomainMapper.selectConversationMessages(7L, "dm-1001")).thenReturn(List.of(messageData));

        Clock fixedClock = Clock.fixed(Instant.parse("2026-04-13T02:35:00Z"), ZoneId.of("Asia/Shanghai"));
        ConversationQueryService service =
                new ConversationQueryService(messageDomainMapper, fixedClock, java.time.Duration.ofMinutes(2));

        ConversationDetailDto detail = service.getConversationDetail(7L, "dm-1001");

        assertEquals("ANONYMOUS_POST", detail.conversationType());
        assertNotNull(detail.peer());
        assertNull(detail.peer().userCode());
        assertEquals("匿名树洞作者", detail.peer().name());
        assertEquals("匿名私信会话", detail.peer().subtitle());
        assertFalse(detail.messages().isEmpty());
        assertEquals("them", detail.messages().get(0).sender());
    }
}
