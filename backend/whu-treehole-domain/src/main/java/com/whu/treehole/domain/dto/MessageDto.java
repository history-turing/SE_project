package com.whu.treehole.domain.dto;

/* 单条消息保持与前端消息气泡结构一致。 */

public record MessageDto(String id, String sender, String text, String time) {
}
