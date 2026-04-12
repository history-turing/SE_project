package com.whu.treehole.domain.dto;

/* Shared message view model for legacy conversation pages and the new DM service. */
public record MessageDto(
        String id,
        String sender,
        String text,
        String time,
        String messageType,
        String status,
        boolean recalled,
        String recalledAt,
        boolean canRecall
) {

    public MessageDto(String id, String sender, String text, String time) {
        this(id, sender, text, time, "TEXT", "SENT", false, null, false);
    }
}
