package com.whu.treehole.message.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import com.whu.treehole.domain.dto.MessageEventDto;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class MessageRealtimeServiceTest {

    @Test
    void shouldPublishCreatedEventAndPushToOnlineUser() {
        RabbitTemplate rabbitTemplate = org.mockito.Mockito.mock(RabbitTemplate.class);
        MessageSessionRegistry registry = new MessageSessionRegistry();
        registry.bindUserSession(9L, "ws-1");

        MessageEventPublisher publisher = new MessageEventPublisher(rabbitTemplate);
        MessageEventConsumer consumer = new MessageEventConsumer(registry);

        MessageEventDto event = new MessageEventDto("message.created", "dm-1001", "msg-1", 7L, List.of(9L));

        publisher.publish(event);
        consumer.handle(event);

        verify(rabbitTemplate).convertAndSend("treehole.dm.exchange", "message.created", event);
        assertEquals(List.of("ws-1"), registry.findSessions(9L));
    }
}
