package com.whu.treehole.message.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.whu.treehole.domain.dto.ConversationListItemDto;
import com.whu.treehole.domain.dto.DirectConversationRequest;
import com.whu.treehole.domain.dto.MessageDto;
import com.whu.treehole.domain.dto.MessageRealtimeEventDto;
import com.whu.treehole.domain.dto.MessageRecipientStateDto;
import com.whu.treehole.domain.dto.UnreadNotificationDto;
import com.whu.treehole.domain.enums.ConversationStatus;
import com.whu.treehole.infra.mapper.MessageDomainMapper;
import com.whu.treehole.infra.model.DmConversationData;
import com.whu.treehole.infra.model.DmConversationParticipantData;
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
class ConversationCommandServiceTest {

    @Mock
    private MessageDomainMapper messageDomainMapper;

    @Mock
    private MessageEventPublisher messageEventPublisher;

    @Mock
    private ConversationQueryService conversationQueryService;

    @Test
    void shouldReuseExistingSingleConversationForSameUsers() {
        DmConversationData existing = new DmConversationData();
        existing.setConversationCode("dm-1001");

        when(messageDomainMapper.selectSingleConversationBetweenUsers(7L, 9L, "DIRECT", null)).thenReturn(existing);

        ConversationCommandService service = new ConversationCommandService(messageDomainMapper);
        String code = service.createOrGetSingleConversation(7L, new DirectConversationRequest("user-9", null, false), 9L);

        assertEquals("dm-1001", code);
        verify(messageDomainMapper).selectSingleConversationBetweenUsers(7L, 9L, "DIRECT", null);
    }

    @Test
    void shouldRejectBlankPeerUserCode() {
        ConversationCommandService service = new ConversationCommandService(messageDomainMapper);

        DirectConversationRequest request = new DirectConversationRequest(" ", null, false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createOrGetSingleConversation(7L, request, 9L));

        assertEquals("peerUserCode required", ex.getMessage());
        verifyNoInteractions(messageDomainMapper);
    }

    @Test
    void shouldCreateSingleConversationWhenMissing() {
        when(messageDomainMapper.selectSingleConversationBetweenUsers(7L, 9L, "DIRECT", null)).thenReturn(null);
        doAnswer(invocation -> {
            DmConversationData data = invocation.getArgument(0);
            data.setId(18L);
            return null;
        }).when(messageDomainMapper).insertConversation(any(DmConversationData.class));

        ConversationCommandService service = new ConversationCommandService(messageDomainMapper);

        String code = service.createOrGetSingleConversation(7L, new DirectConversationRequest("user-9", null, false), 9L);

        assertTrue(code.startsWith("dm-"));
        verify(messageDomainMapper).selectSingleConversationBetweenUsers(7L, 9L, "DIRECT", null);
        verify(messageDomainMapper).insertConversation(any(DmConversationData.class));

        ArgumentCaptor<DmConversationParticipantData> participantCaptor =
                ArgumentCaptor.forClass(DmConversationParticipantData.class);
        verify(messageDomainMapper, times(2)).insertConversationParticipant(participantCaptor.capture());
        assertEquals(18L, participantCaptor.getAllValues().get(0).getConversationId());
        assertEquals(18L, participantCaptor.getAllValues().get(1).getConversationId());
    }

    @Test
    void shouldCreateAnonymousPostConversationWithAnonymousScene() {
        when(messageDomainMapper.selectSingleConversationBetweenUsers(7L, 9L, "ANONYMOUS_POST", "post-1001"))
                .thenReturn(null);
        doAnswer(invocation -> {
            DmConversationData data = invocation.getArgument(0);
            data.setId(19L);
            return null;
        }).when(messageDomainMapper).insertConversation(any(DmConversationData.class));

        ConversationCommandService service = new ConversationCommandService(messageDomainMapper);

        String code = service.createOrGetSingleConversation(
                7L,
                new DirectConversationRequest("user-9", "post-1001", true),
                9L);

        assertTrue(code.startsWith("dm-"));

        ArgumentCaptor<DmConversationData> conversationCaptor = ArgumentCaptor.forClass(DmConversationData.class);
        verify(messageDomainMapper).insertConversation(conversationCaptor.capture());
        assertEquals("ANONYMOUS_POST", conversationCaptor.getValue().getConversationType());
        assertTrue(Boolean.TRUE.equals(conversationCaptor.getValue().getAnonymousFlag()));
        assertEquals("post-1001", conversationCaptor.getValue().getSourcePostCode());
    }

    @Test
    void shouldMarkConversationReadAndPersistLastReadMessage() {
        DmConversationData conversationData = new DmConversationData();
        conversationData.setConversationCode("dm-1001");
        conversationData.setStatus(ConversationStatus.ACTIVE);
        conversationData.setLastMessageId(205L);
        when(messageDomainMapper.selectConversationByCodeAndUserId(7L, "dm-1001")).thenReturn(conversationData);

        DmConversationParticipantData participantData = new DmConversationParticipantData();
        participantData.setUnreadCount(3);
        when(messageDomainMapper.selectConversationParticipant(7L, "dm-1001")).thenReturn(participantData);
        when(conversationQueryService.buildRecipientStates("dm-1001", null))
                .thenReturn(List.of(
                        new MessageRecipientStateDto(
                                7L,
                                new ConversationListItemDto("dm-1001", "DIRECT", "peer", "tagline", "/avatar.png", "last-message", "21:45", 0),
                                null,
                                new UnreadNotificationDto(7L, 0, 0, 0, 0)),
                        new MessageRecipientStateDto(
                                9L,
                                new ConversationListItemDto("dm-1001", "DIRECT", "sender", "tagline", "/avatar.png", "last-message", "21:45", 0),
                                null,
                                new UnreadNotificationDto(9L, 0, 0, 0, 0))));

        Clock fixedClock = Clock.fixed(Instant.parse("2026-04-12T13:45:00Z"), ZoneId.of("Asia/Shanghai"));
        ConversationCommandService service = new ConversationCommandService(
                messageDomainMapper,
                fixedClock,
                messageEventPublisher,
                conversationQueryService);

        service.markConversationRead(7L, "dm-1001");

        verify(messageDomainMapper).markConversationRead(
                7L,
                "dm-1001",
                205L,
                LocalDateTime.of(2026, 4, 12, 21, 45));
        verify(messageEventPublisher).publish(argThat((MessageRealtimeEventDto event) ->
                "conversation.read".equals(event.type())
                        && "dm-1001".equals(event.conversationCode())
                        && event.recipientStates() != null
                        && event.recipientStates().stream().anyMatch(state ->
                                Long.valueOf(7L).equals(state.userId())
                                        && state.message() == null
                                        && state.unreadNotification() != null
                                        && Integer.valueOf(0).equals(state.unreadNotification().messagesUnread()))));
    }
}
