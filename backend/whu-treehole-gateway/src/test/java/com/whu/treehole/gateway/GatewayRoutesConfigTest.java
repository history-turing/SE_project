package com.whu.treehole.gateway;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.whu.treehole.gateway.config.GatewayRoutesConfig;
import org.junit.jupiter.api.Test;

class GatewayRoutesConfigTest {

    @Test
    void shouldExposeApiAndWebSocketRoutes() {
        GatewayRoutesConfig config = new GatewayRoutesConfig();
        String routeSummary = config.routeIds();

        assertTrue(routeSummary.contains("message-service-api"));
        assertTrue(routeSummary.contains("message-service-ws"));
        assertTrue(routeSummary.contains("content-service-api"));
    }
}
