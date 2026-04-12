package com.whu.treehole.message.service;

import com.whu.treehole.domain.dto.DirectConversationRequest;
import com.whu.treehole.domain.enums.ConversationStatus;
import com.whu.treehole.infra.mapper.MessageDomainMapper;
import com.whu.treehole.infra.model.DmConversationData;
import com.whu.treehole.infra.model.DmConversationParticipantData;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConversationCommandService {

    private final MessageDomainMapper messageDomainMapper;

    public ConversationCommandService(MessageDomainMapper messageDomainMapper) {
        this.messageDomainMapper = messageDomainMapper;
    }

    @Transactional
    public String createOrGetSingleConversation(long operatorUserId,
                                                DirectConversationRequest request,
                                                long resolvedPeerUserId) {
        if (request == null) {
            throw new IllegalArgumentException("request required");
        }
        if (request.peerUserCode() == null || request.peerUserCode().isBlank()) {
            throw new IllegalArgumentException("peerUserCode required");
        }
        DmConversationData existing = messageDomainMapper.selectSingleConversationBetweenUsers(operatorUserId, resolvedPeerUserId);
        if (existing != null) {
            return existing.getConversationCode();
        }

        LocalDateTime now = LocalDateTime.now();
        DmConversationData conversationData = new DmConversationData();
        conversationData.setConversationCode("dm-" + System.currentTimeMillis());
        conversationData.setConversationType("SINGLE");
        conversationData.setStatus(ConversationStatus.ACTIVE);
        conversationData.setCreatedBy(operatorUserId);
        conversationData.setCreatedAt(now);
        conversationData.setUpdatedAt(now);
        messageDomainMapper.insertConversation(conversationData);

        if (conversationData.getId() == null) {
            throw new IllegalStateException("conversation id missing after insert");
        }

        DmConversationParticipantData operatorParticipant = new DmConversationParticipantData();
        operatorParticipant.setConversationId(conversationData.getId());
        operatorParticipant.setUserId(operatorUserId);
        operatorParticipant.setUnreadCount(0);
        operatorParticipant.setPinnedFlag(Boolean.FALSE);
        operatorParticipant.setMutedFlag(Boolean.FALSE);
        operatorParticipant.setCreatedAt(now);
        operatorParticipant.setUpdatedAt(now);
        messageDomainMapper.insertConversationParticipant(operatorParticipant);

        DmConversationParticipantData peerParticipant = new DmConversationParticipantData();
        peerParticipant.setConversationId(conversationData.getId());
        peerParticipant.setUserId(resolvedPeerUserId);
        peerParticipant.setUnreadCount(0);
        peerParticipant.setPinnedFlag(Boolean.FALSE);
        peerParticipant.setMutedFlag(Boolean.FALSE);
        peerParticipant.setCreatedAt(now);
        peerParticipant.setUpdatedAt(now);
        messageDomainMapper.insertConversationParticipant(peerParticipant);

        return conversationData.getConversationCode();
    }
}
