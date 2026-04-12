package com.whu.treehole.message.config;

import com.whu.treehole.message.ws.MessageSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class MessageWebSocketConfig implements WebSocketConfigurer {

    private final MessageSocketHandler messageSocketHandler;

    public MessageWebSocketConfig(MessageSocketHandler messageSocketHandler) {
        this.messageSocketHandler = messageSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(messageSocketHandler, "/ws/messages")
                .setAllowedOriginPatterns("*");
    }
}
