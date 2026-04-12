package com.whu.treehole.message.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

    @Test
    void shouldRejectBlankPeerUserCode() {
        ConversationCommandService service = new ConversationCommandService(messageDomainMapper);

        DirectConversationRequest request = new DirectConversationRequest(" ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createOrGetSingleConversation(7L, request, 9L));

        assertEquals("peerUserCode required", ex.getMessage());
        verifyNoInteractions(messageDomainMapper);
    }
}
