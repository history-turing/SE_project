package com.whu.treehole.server.service;

/* 会话服务负责消息发送与已读状态更新。 */

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.dto.MessageCreateRequest;
import com.whu.treehole.domain.dto.MessageDto;
import com.whu.treehole.infra.mapper.PortalCommandMapper;
import com.whu.treehole.infra.model.MessageData;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConversationService {

    private static final DateTimeFormatter MESSAGE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final PortalCommandMapper portalCommandMapper;

    public ConversationService(PortalCommandMapper portalCommandMapper) {
        this.portalCommandMapper = portalCommandMapper;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "profilePage", allEntries = true),
            @CacheEvict(cacheNames = "conversationDetail", allEntries = true)
    })
    public MessageDto sendMessage(long userId, String conversationCode, MessageCreateRequest request) {
        Long conversationId = portalCommandMapper.selectConversationId(userId, conversationCode);
        if (conversationId == null) {
            throw new BusinessException(4042, "会话不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        String displayTime = now.format(MESSAGE_TIME_FORMATTER);
        MessageData messageData = new MessageData();
        messageData.setConversationId(conversationId);
        messageData.setMessageCode(conversationCode + "-" + System.currentTimeMillis());
        messageData.setSenderType("ME");
        messageData.setTextContent(request.text().trim());
        messageData.setDisplayTime(displayTime);
        messageData.setCreatedAt(now);
        portalCommandMapper.insertMessage(messageData);
        portalCommandMapper.updateConversationAfterSend(conversationId, request.text().trim(), displayTime, now);
        portalCommandMapper.markConversationRead(userId, conversationCode);

        return new MessageDto(messageData.getMessageCode(), "me", messageData.getTextContent(), displayTime);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "profilePage", allEntries = true),
            @CacheEvict(cacheNames = "conversationDetail", allEntries = true)
    })
    public void markRead(long userId, String conversationCode) {
        Long conversationId = portalCommandMapper.selectConversationId(userId, conversationCode);
        if (conversationId == null) {
            throw new BusinessException(4042, "会话不存在");
        }
        portalCommandMapper.markConversationRead(userId, conversationCode);
    }
}
