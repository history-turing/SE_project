package com.whu.treehole.message.config;

import java.time.Clock;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageServiceConfig {

    public static final String DM_EXCHANGE = "treehole.dm.exchange";
    public static final String MESSAGE_CREATED_QUEUE = "treehole.dm.message.created.queue";

    @Bean
    TopicExchange dmExchange() {
        return new TopicExchange(DM_EXCHANGE, true, false);
    }

    @Bean
    Queue messageCreatedQueue() {
        return new Queue(MESSAGE_CREATED_QUEUE, true);
    }

    @Bean
    Binding messageCreatedBinding(Queue messageCreatedQueue, TopicExchange dmExchange) {
        return BindingBuilder.bind(messageCreatedQueue).to(dmExchange).with("message.created");
    }

    @Bean
    MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}
