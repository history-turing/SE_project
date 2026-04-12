package com.whu.treehole.message.service;

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.dto.DirectConversationRequest;
import com.whu.treehole.domain.enums.ConversationStatus;
import com.whu.treehole.infra.mapper.MessageDomainMapper;
import com.whu.treehole.infra.model.DmConversationData;
import com.whu.treehole.infra.model.DmConversationParticipantData;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConversationCommandService {

    private final MessageDomainMapper messageDomainMapper;
    private final Clock clock;

    ConversationCommandService(MessageDomainMapper messageDomainMapper) {
        this(messageDomainMapper, Clock.systemDefaultZone());
    }

    @Autowired
    public ConversationCommandService(MessageDomainMapper messageDomainMapper, Clock clock) {
        this.messageDomainMapper = messageDomainMapper;
        this.clock = clock;
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

        LocalDateTime now = LocalDateTime.now(clock);
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

    @Transactional
    public void markConversationRead(long operatorUserId, String conversationCode) {
        DmConversationData conversationData =
                messageDomainMapper.selectConversationByCodeAndUserId(operatorUserId, conversationCode);
        if (conversationData == null) {
            throw new BusinessException(4042, "会话不存在");
        }

        DmConversationParticipantData participantData =
                messageDomainMapper.selectConversationParticipant(operatorUserId, conversationCode);
        if (participantData == null) {
            throw new BusinessException(4042, "会话不存在");
        }
        if (participantData.getUnreadCount() == null || participantData.getUnreadCount() <= 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now(clock);
        messageDomainMapper.markConversationRead(
                operatorUserId,
                conversationCode,
                conversationData.getLastMessageId(),
                now);
    }
}
