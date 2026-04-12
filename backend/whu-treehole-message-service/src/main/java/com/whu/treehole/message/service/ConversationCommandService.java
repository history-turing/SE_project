package com.whu.treehole.message.service;

import com.whu.treehole.domain.dto.DirectConversationRequest;
import com.whu.treehole.infra.mapper.MessageDomainMapper;
import com.whu.treehole.infra.model.DmConversationData;
import org.springframework.stereotype.Service;

@Service
public class ConversationCommandService {

    private final MessageDomainMapper messageDomainMapper;

    public ConversationCommandService(MessageDomainMapper messageDomainMapper) {
        this.messageDomainMapper = messageDomainMapper;
    }

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
        throw new UnsupportedOperationException("create path implemented in Task 2");
    }
}
