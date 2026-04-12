package com.whu.treehole.gateway.config;

import java.util.List;
import java.util.StringJoiner;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    public static final String MESSAGE_SERVICE_API_ROUTE = "message-service-api";
    public static final String MESSAGE_SERVICE_WS_ROUTE = "message-service-ws";
    public static final String CONTENT_SERVICE_API_ROUTE = "content-service-api";

    @Bean
    RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(CONTENT_SERVICE_API_ROUTE, route -> route
                        .path("/api/v1/**")
                        .and()
                        .not(predicate -> predicate.path("/api/v1/dm/**"))
                        .uri("lb://whu-treehole-backend"))
                .route(MESSAGE_SERVICE_API_ROUTE, route -> route
                        .path("/api/v1/dm/**")
                        .uri("lb://whu-treehole-message-service"))
                .route(MESSAGE_SERVICE_WS_ROUTE, route -> route
                        .path("/ws/messages/**")
                        .uri("lb:ws://whu-treehole-message-service"))
                .build();
    }

    public String routeIds() {
        StringJoiner joiner = new StringJoiner(",");
        routeIdList().forEach(joiner::add);
        return joiner.toString();
    }

    public List<String> routeIdList() {
        return List.of(
                MESSAGE_SERVICE_API_ROUTE,
                MESSAGE_SERVICE_WS_ROUTE,
                CONTENT_SERVICE_API_ROUTE
        );
    }
}
