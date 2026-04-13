package com.whu.treehole.server.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.whu.treehole.server.support.AuthInterceptor;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.cors.CorsConfiguration;

class WebConfigCorsTest {

    @Test
    void shouldAllowProductionOriginForPublicWebSite() throws Exception {
        WebConfig webConfig = new WebConfig(mock(AuthInterceptor.class), new WebCorsProperties());
        CorsRegistry registry = new CorsRegistry();

        webConfig.addCorsMappings(registry);

        List<CorsRegistration> registrations = readField(registry, "registrations");
        assertEquals(1, registrations.size());

        CorsConfiguration configuration = readField(registrations.get(0), "config");
        assertTrue(configuration.getAllowedOrigins().contains("http://43.134.116.122"));
    }

    @SuppressWarnings("unchecked")
    private static <T> T readField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }
}
