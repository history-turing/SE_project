package com.whu.treehole.gateway.auth;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthHeaderPropagationFilter implements GlobalFilter, Ordered {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String SESSION_KEY_PREFIX = "auth:session:";
    private static final String WS_TOKEN_PARAM = "token";

    private final StringRedisTemplate stringRedisTemplate;

    public AuthHeaderPropagationFilter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (!requiresUserPropagation(path) || exchange.getRequest().getHeaders().containsKey(USER_ID_HEADER)) {
            return chain.filter(exchange);
        }

        String token = resolveToken(exchange);
        if (token == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String userId = stringRedisTemplate.opsForValue().get(SESSION_KEY_PREFIX + token);
        if (userId == null || userId.isBlank()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        ServerWebExchange nextExchange = exchange.mutate()
                .request(request -> request.headers(headers -> headers.set(USER_ID_HEADER, userId.trim())))
                .build();
        return chain.filter(nextExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean requiresUserPropagation(String path) {
        return path.startsWith("/api/v1/dm/") || "/ws/messages".equals(path) || path.startsWith("/ws/messages/");
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private String resolveToken(ServerWebExchange exchange) {
        String bearerToken = extractBearerToken(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        if (bearerToken != null) {
            return bearerToken;
        }
        String queryToken = exchange.getRequest().getQueryParams().getFirst(WS_TOKEN_PARAM);
        if (queryToken == null || queryToken.isBlank()) {
            return null;
        }
        return queryToken.trim();
    }
}
