package com.whu.treehole.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.whu.treehole.gateway.auth.AuthHeaderPropagationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

class AuthHeaderPropagationFilterTest {

    @Test
    void shouldInjectUserHeaderForDmRequest() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:session:test-token")).thenReturn("7");

        AuthHeaderPropagationFilter filter = new AuthHeaderPropagationFilter(redisTemplate);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/dm/conversations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .build());

        GatewayFilterChain chain = nextExchange -> {
            assertEquals("7", nextExchange.getRequest().getHeaders().getFirst("X-User-Id"));
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        verify(valueOperations).get("auth:session:test-token");
    }

    @Test
    void shouldRejectDmRequestWithoutValidSession() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:session:expired-token")).thenReturn(null);

        AuthHeaderPropagationFilter filter = new AuthHeaderPropagationFilter(redisTemplate);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/dm/conversations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer expired-token")
                        .build());

        filter.filter(exchange, ignored -> Mono.error(new IllegalStateException("chain should not run"))).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldInjectUserHeaderForWebSocketHandshakeUsingQueryToken() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:session:ws-token")).thenReturn("7");

        AuthHeaderPropagationFilter filter = new AuthHeaderPropagationFilter(redisTemplate);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/ws/messages?token=ws-token").build());

        GatewayFilterChain chain = nextExchange -> {
            assertEquals("7", nextExchange.getRequest().getHeaders().getFirst("X-User-Id"));
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        verify(valueOperations).get("auth:session:ws-token");
    }
}
