package com.whu.treehole.message.service;

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.dto.DirectConversationRequest;
import com.whu.treehole.domain.dto.MessageRealtimeEventDto;
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
    private final MessageEventPublisher messageEventPublisher;
    private final ConversationQueryService conversationQueryService;

    ConversationCommandService(MessageDomainMapper messageDomainMapper) {
        this(messageDomainMapper, Clock.systemDefaultZone(), null, null);
    }

    @Autowired
    public ConversationCommandService(MessageDomainMapper messageDomainMapper,
                                      Clock clock,
                                      MessageEventPublisher messageEventPublisher,
                                      ConversationQueryService conversationQueryService) {
        this.messageDomainMapper = messageDomainMapper;
        this.clock = clock;
        this.messageEventPublisher = messageEventPublisher;
        this.conversationQueryService = conversationQueryService;
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
        boolean anonymousScene = isAnonymousScene(request);
        String conversationScene = anonymousScene ? "ANONYMOUS_POST" : "DIRECT";
        DmConversationData existing = messageDomainMapper.selectSingleConversationBetweenUsers(
                operatorUserId,
                resolvedPeerUserId,
                conversationScene,
                anonymousScene ? request.sourcePostCode().trim() : null);
        if (existing != null) {
            return existing.getConversationCode();
        }

        LocalDateTime now = LocalDateTime.now(clock);
        DmConversationData conversationData = new DmConversationData();
        conversationData.setConversationCode("dm-" + System.currentTimeMillis());
        conversationData.setConversationType(conversationScene);
        conversationData.setConversationScene(conversationScene);
        conversationData.setSourcePostCode(anonymousScene ? request.sourcePostCode().trim() : null);
        conversationData.setAnonymousFlag(anonymousScene);
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

    private boolean isAnonymousScene(DirectConversationRequest request) {
        return request.anonymousEntry()
                && request.sourcePostCode() != null
                && !request.sourcePostCode().isBlank();
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
        publishReadEvent(conversationCode);
    }

    private void publishReadEvent(String conversationCode) {
        if (messageEventPublisher == null || conversationQueryService == null) {
            return;
        }
        messageEventPublisher.publish(new MessageRealtimeEventDto(
                "conversation.read",
                conversationCode,
                conversationQueryService.buildRecipientStates(conversationCode, null)));
    }
}
