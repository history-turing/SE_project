package com.whu.treehole.message.service;

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.dto.MessageDto;
import com.whu.treehole.domain.dto.MessageSendRequest;
import com.whu.treehole.domain.enums.MessageStatus;
import com.whu.treehole.domain.enums.MessageType;
import com.whu.treehole.infra.mapper.MessageDomainMapper;
import com.whu.treehole.infra.model.DmConversationParticipantData;
import com.whu.treehole.infra.model.DmMessageData;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageCommandService {

    private static final DateTimeFormatter MESSAGE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final MessageDomainMapper messageDomainMapper;
    private final Clock clock;

    public MessageCommandService(MessageDomainMapper messageDomainMapper) {
        this(messageDomainMapper, Clock.system(Clock.systemDefaultZone().getZone()));
    }

    MessageCommandService(MessageDomainMapper messageDomainMapper, Clock clock) {
        this.messageDomainMapper = messageDomainMapper;
        this.clock = clock;
    }

    @Transactional
    public MessageDto sendMessage(long operatorUserId, String conversationCode, MessageSendRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request required");
        }

        String trimmedContent = request.content() == null ? null : request.content().trim();
        if (trimmedContent == null || trimmedContent.isBlank()) {
            throw new IllegalArgumentException("content required");
        }

        Long conversationId = messageDomainMapper.selectConversationIdByCode(conversationCode);
        if (conversationId == null) {
            throw new BusinessException(4042, "会话不存在");
        }
        DmConversationParticipantData participantData =
                messageDomainMapper.selectConversationParticipant(operatorUserId, conversationCode);
        if (participantData == null) {
            throw new BusinessException(4042, "会话不存在");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        DmMessageData messageData = new DmMessageData();
        messageData.setMessageCode(conversationCode + "-" + System.currentTimeMillis());
        messageData.setClientMessageId(request.clientMessageId());
        messageData.setConversationId(conversationId);
        messageData.setSenderUserId(operatorUserId);
        messageData.setMessageType(MessageType.TEXT);
        messageData.setStatus(MessageStatus.SENT);
        messageData.setContentPayload(trimmedContent);
        messageData.setSentAt(now);
        messageData.setCreatedAt(now);
        messageData.setUpdatedAt(now);

        messageDomainMapper.insertMessage(messageData);
        messageDomainMapper.updateConversationAfterSend(conversationId, messageData.getId(), now);
        messageDomainMapper.increaseUnreadForPeer(conversationId, operatorUserId);

        return new MessageDto(
                messageData.getMessageCode(),
                "me",
                trimmedContent,
                now.format(MESSAGE_TIME_FORMATTER)
        );
    }
}
