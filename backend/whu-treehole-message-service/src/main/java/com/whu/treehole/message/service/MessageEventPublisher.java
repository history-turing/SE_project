package com.whu.treehole.message.service;

import com.whu.treehole.domain.dto.MessageEventDto;
import com.whu.treehole.message.config.MessageServiceConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public MessageEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(MessageEventDto event) {
        rabbitTemplate.convertAndSend(MessageServiceConfig.DM_EXCHANGE, event.type(), event);
    }
}
