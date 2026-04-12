package com.whu.treehole.message.service;

import com.whu.treehole.domain.dto.MessageEventDto;
import com.whu.treehole.message.config.MessageServiceConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessageEventConsumer {

    private final MessageSessionRegistry messageSessionRegistry;

    public MessageEventConsumer(MessageSessionRegistry messageSessionRegistry) {
        this.messageSessionRegistry = messageSessionRegistry;
    }

    @RabbitListener(queues = MessageServiceConfig.MESSAGE_CREATED_QUEUE)
    public void handle(MessageEventDto event) {
        messageSessionRegistry.pushEvent(event);
    }
}
