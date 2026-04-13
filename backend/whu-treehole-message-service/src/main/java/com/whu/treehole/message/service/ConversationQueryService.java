package com.whu.treehole.message.service;

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.dto.ConversationDetailDto;
import com.whu.treehole.domain.dto.ConversationListItemDto;
import com.whu.treehole.domain.dto.ConversationPeerDto;
import com.whu.treehole.domain.dto.MessageDto;
import com.whu.treehole.domain.dto.MessageRecipientStateDto;
import com.whu.treehole.domain.dto.UnreadNotificationDto;
import com.whu.treehole.domain.enums.MessageStatus;
import com.whu.treehole.domain.enums.MessageType;
import com.whu.treehole.infra.mapper.MessageDomainMapper;
import com.whu.treehole.infra.model.ConversationData;
import com.whu.treehole.infra.model.DmConversationData;
import com.whu.treehole.infra.model.DmConversationParticipantData;
import com.whu.treehole.infra.model.DmMessageData;
import com.whu.treehole.infra.model.DmUnreadAggregateData;
import com.whu.treehole.infra.model.UserProfileData;
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
public class ConversationQueryService {

    private static final DateTimeFormatter MESSAGE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final MessageDomainMapper messageDomainMapper;
    private final Clock clock;
    private final Duration recallWindow;

    ConversationQueryService(MessageDomainMapper messageDomainMapper) {
        this(messageDomainMapper, Clock.systemDefaultZone(), Duration.ofMinutes(2));
    }

    @Autowired
    public ConversationQueryService(MessageDomainMapper messageDomainMapper,
                                    Clock clock,
                                    @Value("${treehole.messaging.recall-window-seconds:120}") long recallWindowSeconds) {
        this(messageDomainMapper, clock, Duration.ofSeconds(Math.max(30L, recallWindowSeconds)));
    }

    ConversationQueryService(MessageDomainMapper messageDomainMapper, Clock clock, Duration recallWindow) {
        this.messageDomainMapper = messageDomainMapper;
        this.clock = clock;
        this.recallWindow = recallWindow;
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
                normalizeConversationType(conversationData),
                conversationData.getStatus(),
                toPeerDto(peerData, conversationData),
                lastMessage,
                lastMessageTime,
                unreadCount,
                messages
        );
    }

    @Transactional(readOnly = true)
    public ConversationListItemDto getConversationSummary(long userId, String conversationCode) {
        ConversationData data = messageDomainMapper.selectConversationSummary(userId, conversationCode);
        if (data == null) {
            throw new BusinessException(4042, "浼氳瘽涓嶅瓨鍦?");
        }
        return toConversationListItem(data);
    }

    @Transactional(readOnly = true)
    public UnreadNotificationDto getUnreadNotification(long userId) {
        DmUnreadAggregateData aggregate = messageDomainMapper.selectUnreadAggregate(userId);
        int messagesUnread = aggregate == null || aggregate.getMessagesUnread() == null ? 0 : aggregate.getMessagesUnread();
        return new UnreadNotificationDto((long) userId, messagesUnread, 0, 0, messagesUnread);
    }

    @Transactional(readOnly = true)
    public List<MessageRecipientStateDto> buildRecipientStates(String conversationCode, DmMessageData messageData) {
        return messageDomainMapper.selectConversationParticipantUserIds(conversationCode).stream()
                .map(userId -> new MessageRecipientStateDto(
                        userId,
                        getConversationSummary(userId, conversationCode),
                        messageData == null ? null : toMessageDto(userId, messageData),
                        getUnreadNotification(userId)))
                .toList();
    }

    private ConversationListItemDto toConversationListItem(ConversationData data) {
        return new ConversationListItemDto(
                data.getConversationCode(),
                normalizeConversationType(data.getConversationType()),
                data.getPeerName(),
                data.getPeerSubtitle(),
                data.getPeerAvatarUrl(),
                data.getLastMessage(),
                data.getDisplayTime(),
                data.getUnreadCount()
        );
    }

    private ConversationPeerDto toPeerDto(UserProfileData data, DmConversationData conversationData) {
        if (isAnonymousConversation(conversationData)) {
            return new ConversationPeerDto(null, "匿名树洞作者", "匿名私信会话", "/images/avatar-anonymous.svg");
        }
        if (data == null) {
            return null;
        }
        return new ConversationPeerDto(data.getUserCode(), data.getName(), data.getTagline(), data.getAvatarUrl());
    }

    private String normalizeConversationType(DmConversationData conversationData) {
        if (conversationData == null) {
            return "DIRECT";
        }
        return normalizeConversationType(conversationData.getConversationType());
    }

    private String normalizeConversationType(String conversationType) {
        if (conversationType == null || conversationType.isBlank() || "SINGLE".equalsIgnoreCase(conversationType)) {
            return "DIRECT";
        }
        return conversationType;
    }

    private boolean isAnonymousConversation(DmConversationData conversationData) {
        return conversationData != null
                && (Boolean.TRUE.equals(conversationData.getAnonymousFlag())
                || "ANONYMOUS_POST".equalsIgnoreCase(conversationData.getConversationScene())
                || "ANONYMOUS_POST".equalsIgnoreCase(conversationData.getConversationType()));
    }

    private MessageDto toMessageDto(long userId, DmMessageData data) {
        String sender = data.getSenderUserId() != null && data.getSenderUserId() == userId ? "me" : "them";
        boolean recalled = data.getStatus() == MessageStatus.REVOKED;
        String text = recalled
                ? ("me".equals(sender) ? "你撤回了一条消息" : "对方撤回了一条消息")
                : data.getContentPayload();
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
