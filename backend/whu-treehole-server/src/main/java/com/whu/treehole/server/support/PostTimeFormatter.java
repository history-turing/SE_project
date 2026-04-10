package com.whu.treehole.server.support;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class PostTimeFormatter {

    private static final DateTimeFormatter POST_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String format(LocalDateTime createdAt, String fallbackDisplayTime) {
        if (createdAt == null) {
            return fallbackDisplayTime;
        }
        return createdAt.format(POST_TIME_FORMATTER);
    }
}
