package com.whu.treehole.message.controller;

import com.whu.treehole.common.api.ApiResponse;
import com.whu.treehole.domain.dto.ConversationDetailDto;
import com.whu.treehole.domain.dto.ConversationListItemDto;
import com.whu.treehole.domain.dto.DirectConversationRequest;
import com.whu.treehole.domain.dto.MessageDto;
import com.whu.treehole.domain.dto.MessageSendRequest;
import com.whu.treehole.infra.mapper.AuthMapper;
import com.whu.treehole.message.service.ConversationCommandService;
import com.whu.treehole.message.service.ConversationQueryService;
import com.whu.treehole.message.service.MessageCommandService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dm/conversations")
public class MessageConversationController {

    private final AuthMapper authMapper;
    private final ConversationCommandService conversationCommandService;
    private final ConversationQueryService conversationQueryService;
    private final MessageCommandService messageCommandService;

    public MessageConversationController(AuthMapper authMapper,
                                         ConversationCommandService conversationCommandService,
                                         ConversationQueryService conversationQueryService,
                                         MessageCommandService messageCommandService) {
        this.authMapper = authMapper;
        this.conversationCommandService = conversationCommandService;
        this.conversationQueryService = conversationQueryService;
        this.messageCommandService = messageCommandService;
    }

    @GetMapping
    public ApiResponse<List<ConversationListItemDto>> listConversations(
            @RequestHeader("X-User-Id") long userId) {
        return ApiResponse.success(conversationQueryService.listConversations(userId));
    }

    @GetMapping("/{conversationCode}")
    public ApiResponse<ConversationDetailDto> getConversationDetail(
            @RequestHeader("X-User-Id") long userId,
            @PathVariable String conversationCode) {
        return ApiResponse.success(conversationQueryService.getConversationDetail(userId, conversationCode));
    }

    @PostMapping("/direct")
    public ApiResponse<String> createOrGetDirectConversation(
            @RequestHeader("X-User-Id") long userId,
            @Valid @RequestBody DirectConversationRequest request) {
        Long peerUserId = authMapper.selectUserIdByUserCode(request.peerUserCode());
        if (peerUserId == null) {
            throw new IllegalArgumentException("peer user not found");
        }
        if (peerUserId == userId) {
            throw new IllegalArgumentException("cannot create conversation with self");
        }
        return ApiResponse.success(conversationCommandService.createOrGetSingleConversation(userId, request, peerUserId));
    }

    @PostMapping("/{conversationCode}/messages")
    public ApiResponse<MessageDto> sendMessage(@RequestHeader("X-User-Id") long userId,
                                               @PathVariable String conversationCode,
                                               @Valid @RequestBody MessageSendRequest request) {
        return ApiResponse.success(messageCommandService.sendMessage(userId, conversationCode, request));
    }

    @PostMapping("/{conversationCode}/messages/{messageCode}/recall")
    public ApiResponse<MessageDto> recallMessage(@RequestHeader("X-User-Id") long userId,
                                                 @PathVariable String conversationCode,
                                                 @PathVariable String messageCode) {
        return ApiResponse.success(messageCommandService.recallMessage(userId, conversationCode, messageCode));
    }

    @PostMapping("/{conversationCode}/read")
    public ApiResponse<Void> markConversationRead(@RequestHeader("X-User-Id") long userId,
                                                  @PathVariable String conversationCode) {
        conversationCommandService.markConversationRead(userId, conversationCode);
        return ApiResponse.success(null);
    }
}
