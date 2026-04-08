package com.whu.treehole.server.controller;

/* 会话控制器负责发送消息和清空未读数。 */

import com.whu.treehole.common.api.ApiResponse;
import com.whu.treehole.domain.dto.MessageCreateRequest;
import com.whu.treehole.domain.dto.MessageDto;
import com.whu.treehole.server.service.ConversationService;
import com.whu.treehole.server.support.AuthContextHolder;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping("/{conversationCode}/messages")
    public ApiResponse<MessageDto> sendMessage(@PathVariable String conversationCode,
                                               @Valid @RequestBody MessageCreateRequest request) {
        return ApiResponse.success(conversationService.sendMessage(
                AuthContextHolder.currentUserId(), conversationCode, request));
    }

    @PostMapping("/{conversationCode}/read")
    public ApiResponse<Void> markConversationRead(@PathVariable String conversationCode) {
        conversationService.markRead(AuthContextHolder.currentUserId(), conversationCode);
        return ApiResponse.success(null);
    }
}
