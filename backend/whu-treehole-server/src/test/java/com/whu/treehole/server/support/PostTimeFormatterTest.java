package com.whu.treehole.server.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PostTimeFormatterTest {

    private final PostTimeFormatter formatter = new PostTimeFormatter();

    @Test
    void shouldFormatCreatedAtAsExactTimestamp() {
        String displayTime = formatter.format(LocalDateTime.of(2026, 4, 11, 9, 5), "刚刚");

        assertEquals("2026-04-11 09:05", displayTime);
    }

    @Test
    void shouldFallbackWhenCreatedAtIsMissing() {
        String displayTime = formatter.format(null, "刚刚");

        assertEquals("刚刚", displayTime);
    }
}
