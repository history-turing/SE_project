package com.whu.treehole.message.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import com.whu.treehole.domain.dto.ConversationListItemDto;
import com.whu.treehole.domain.dto.MessageDto;
import com.whu.treehole.domain.dto.MessageRealtimeEventDto;
import com.whu.treehole.domain.dto.MessageRecipientStateDto;
import com.whu.treehole.domain.dto.UnreadNotificationDto;
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

        MessageRealtimeEventDto event = new MessageRealtimeEventDto(
                "message.created",
                "dm-1001",
                List.of(new MessageRecipientStateDto(
                        9L,
                        new ConversationListItemDto("dm-1001", "DIRECT", "对方", "签名", "/avatar.png", "hello", "21:45", 1),
                        new MessageDto("msg-1", "them", "hello", "21:45", "TEXT", "SENT", false, null, false),
                        new UnreadNotificationDto(9L, 1, 0, 0, 1))));

        publisher.publish(event);
        consumer.handle(event);

        verify(rabbitTemplate).convertAndSend("treehole.dm.exchange", "message.created", event);
        assertEquals(List.of("ws-1"), registry.findSessions(9L));
    }
}
