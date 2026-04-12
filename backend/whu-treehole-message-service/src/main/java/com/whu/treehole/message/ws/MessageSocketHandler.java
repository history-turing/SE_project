package com.whu.treehole.message.ws;

import com.whu.treehole.message.service.MessageSessionRegistry;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class MessageSocketHandler extends TextWebSocketHandler {

    private final MessageSessionRegistry messageSessionRegistry;

    public MessageSocketHandler(MessageSessionRegistry messageSessionRegistry) {
        this.messageSessionRegistry = messageSessionRegistry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = resolveUserId(session);
        if (userId == null) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("missing X-User-Id header"));
            return;
        }
        messageSessionRegistry.bindWebSocketSession(userId, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        messageSessionRegistry.unbindSession(session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Phase C keeps client-to-server websocket payloads minimal; sending still uses HTTP API.
    }

    private Long resolveUserId(WebSocketSession session) {
        List<String> headers = session.getHandshakeHeaders().get("X-User-Id");
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(headers.get(0));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
