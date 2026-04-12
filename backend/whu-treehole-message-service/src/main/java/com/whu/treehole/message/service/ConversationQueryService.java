package com.whu.treehole.message.service;

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.dto.ConversationDetailDto;
import com.whu.treehole.domain.dto.ConversationListItemDto;
import com.whu.treehole.domain.dto.ConversationPeerDto;
import com.whu.treehole.domain.dto.MessageDto;
import com.whu.treehole.infra.mapper.MessageDomainMapper;
import com.whu.treehole.infra.model.ConversationData;
import com.whu.treehole.infra.model.DmConversationData;
import com.whu.treehole.infra.model.DmConversationParticipantData;
import com.whu.treehole.infra.model.DmMessageData;
import com.whu.treehole.infra.model.UserProfileData;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConversationQueryService {

    private static final DateTimeFormatter MESSAGE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final MessageDomainMapper messageDomainMapper;

    public ConversationQueryService(MessageDomainMapper messageDomainMapper) {
        this.messageDomainMapper = messageDomainMapper;
    }

    @Transactional(readOnly = true)
    public List<ConversationListItemDto> listConversations(long userId) {
        return messageDomainMapper.selectConversationList(userId).stream()
                .map(this::toConversationListItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConversationDetailDto getConversationDetail(long userId, String conversationCode) {
        DmConversationData conversationData = messageDomainMapper.selectConversationByCodeAndUserId(userId, conversationCode);
        if (conversationData == null) {
            throw new BusinessException(4042, "会话不存在");
        }

        DmConversationParticipantData participantData =
                messageDomainMapper.selectConversationParticipant(userId, conversationCode);
        UserProfileData peerData = messageDomainMapper.selectConversationPeer(userId, conversationCode);
        List<MessageDto> messages = messageDomainMapper.selectConversationMessages(userId, conversationCode).stream()
                .map(message -> toMessageDto(userId, message))
                .toList();

        String lastMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1).text();
        String lastMessageTime = messages.isEmpty() ? null : messages.get(messages.size() - 1).time();
        Integer unreadCount = participantData == null ? 0 : participantData.getUnreadCount();

        return new ConversationDetailDto(
                conversationData.getConversationCode(),
                conversationData.getStatus(),
                toPeerDto(peerData),
                lastMessage,
                lastMessageTime,
                unreadCount,
                messages
        );
    }

    private ConversationListItemDto toConversationListItem(ConversationData data) {
        return new ConversationListItemDto(
                data.getConversationCode(),
                data.getPeerName(),
                data.getPeerSubtitle(),
                data.getPeerAvatarUrl(),
                data.getLastMessage(),
                data.getDisplayTime(),
                data.getUnreadCount()
        );
    }

    private ConversationPeerDto toPeerDto(UserProfileData data) {
        if (data == null) {
            return null;
        }
        return new ConversationPeerDto(data.getUserCode(), data.getName(), data.getTagline(), data.getAvatarUrl());
    }

    private MessageDto toMessageDto(long userId, DmMessageData data) {
        return new MessageDto(
                data.getMessageCode(),
                data.getSenderUserId() != null && data.getSenderUserId() == userId ? "me" : "them",
                data.getContentPayload(),
                data.getSentAt() == null ? null : data.getSentAt().format(MESSAGE_TIME_FORMATTER)
        );
    }
}
