package com.whu.treehole.message.service;

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.dto.MessageDto;
import com.whu.treehole.domain.dto.MessageRealtimeEventDto;
import com.whu.treehole.domain.dto.MessageSendRequest;
import com.whu.treehole.domain.enums.MessageStatus;
import com.whu.treehole.domain.enums.MessageType;
import com.whu.treehole.infra.mapper.MessageDomainMapper;
import com.whu.treehole.infra.model.DmConversationParticipantData;
import com.whu.treehole.infra.model.DmMessageData;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageCommandService {

    private static final DateTimeFormatter MESSAGE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final MessageDomainMapper messageDomainMapper;
    private final Clock clock;
    private final MessageEventPublisher messageEventPublisher;
    private final ConversationQueryService conversationQueryService;
    private final Duration recallWindow;

    MessageCommandService(MessageDomainMapper messageDomainMapper) {
        this(messageDomainMapper, Clock.systemDefaultZone(), null, null, Duration.ofMinutes(2));
    }

    MessageCommandService(MessageDomainMapper messageDomainMapper, Clock clock) {
        this(messageDomainMapper, clock, null, null, Duration.ofMinutes(2));
    }

    @Autowired
    public MessageCommandService(MessageDomainMapper messageDomainMapper,
                                 Clock clock,
                                 MessageEventPublisher messageEventPublisher,
                                 ConversationQueryService conversationQueryService,
                                 @Value("${treehole.messaging.recall-window-seconds:120}") long recallWindowSeconds) {
        this(
                messageDomainMapper,
                clock,
                messageEventPublisher,
                conversationQueryService,
                Duration.ofSeconds(Math.max(30L, recallWindowSeconds)));
    }

    MessageCommandService(MessageDomainMapper messageDomainMapper,
                          Clock clock,
                          MessageEventPublisher messageEventPublisher,
                          ConversationQueryService conversationQueryService,
                          Duration recallWindow) {
        this.messageDomainMapper = messageDomainMapper;
        this.clock = clock;
        this.messageEventPublisher = messageEventPublisher;
        this.conversationQueryService = conversationQueryService;
        this.recallWindow = recallWindow;
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
        publishEvent("message.created", conversationCode, messageData);

        return toMessageDto(operatorUserId, messageData);
    }

    @Transactional
    public MessageDto recallMessage(long operatorUserId, String conversationCode, String messageCode) {
        DmConversationParticipantData participantData =
                messageDomainMapper.selectConversationParticipant(operatorUserId, conversationCode);
        if (participantData == null) {
            throw new BusinessException(4042, "会话不存在");
        }

        DmMessageData messageData = messageDomainMapper.selectMessageByCode(operatorUserId, conversationCode, messageCode);
        if (messageData == null) {
            throw new BusinessException(4044, "消息不存在");
        }
        if (messageData.getSenderUserId() == null || messageData.getSenderUserId() != operatorUserId) {
            throw new BusinessException(4003, "仅支持撤回自己发送的消息");
        }
        if (messageData.getStatus() == MessageStatus.REVOKED) {
            return toMessageDto(operatorUserId, messageData);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        if (messageData.getSentAt() == null || Duration.between(messageData.getSentAt(), now).compareTo(recallWindow) > 0) {
            throw new BusinessException(4004, "消息已超过可撤回时限");
        }

        messageData.setStatus(MessageStatus.REVOKED);
        messageData.setRecalledAt(now);
        messageData.setUpdatedAt(now);
        messageDomainMapper.recallMessage(messageData.getId(), MessageStatus.REVOKED, now);
        publishEvent("message.recalled", conversationCode, messageData);
        return toMessageDto(operatorUserId, messageData);
    }

    private void publishEvent(String type, String conversationCode, DmMessageData messageData) {
        if (messageEventPublisher == null || conversationQueryService == null) {
            return;
        }
        List<com.whu.treehole.domain.dto.MessageRecipientStateDto> recipientStates =
                conversationQueryService.buildRecipientStates(conversationCode, messageData);
        if (recipientStates == null || recipientStates.isEmpty()) {
            return;
        }
        messageEventPublisher.publish(new MessageRealtimeEventDto(
                type,
                conversationCode,
                recipientStates
        ));
    }

    private MessageDto toMessageDto(long operatorUserId, DmMessageData data) {
        String sender = data.getSenderUserId() != null && data.getSenderUserId() == operatorUserId ? "me" : "them";
        boolean recalled = data.getStatus() == MessageStatus.REVOKED;
        String text;
        if (recalled) {
            text = "me".equals(sender) ? "你撤回了一条消息" : "对方撤回了一条消息";
        } else {
            text = data.getContentPayload();
        }
        boolean canRecall = "me".equals(sender)
                && !recalled
                && data.getSentAt() != null
                && Duration.between(data.getSentAt(), LocalDateTime.now(clock)).compareTo(recallWindow) <= 0;
        return new MessageDto(
                data.getMessageCode(),
                sender,
                text,
                data.getSentAt() == null ? null : data.getSentAt().format(MESSAGE_TIME_FORMATTER),
                data.getMessageType() == null ? MessageType.TEXT.name() : data.getMessageType().name(),
                data.getStatus() == null ? MessageStatus.SENT.name() : data.getStatus().name(),
                recalled,
                data.getRecalledAt() == null ? null : data.getRecalledAt().toString(),
                canRecall
        );
    }
}
