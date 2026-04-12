package com.whu.treehole.message.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whu.treehole.domain.dto.MessageEventDto;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public class MessageSessionRegistry {

    private static final Duration SESSION_TTL = Duration.ofHours(4);

    private final Map<Long, CopyOnWriteArrayList<String>> userSessions = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionOwners = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> webSocketSessions = new ConcurrentHashMap<>();
    private final Map<String, CopyOnWriteArrayList<MessageEventDto>> pendingEvents = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private StringRedisTemplate redisTemplate;

    @Autowired(required = false)
    void setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void bindUserSession(long userId, String sessionId) {
        userSessions.computeIfAbsent(userId, ignored -> new CopyOnWriteArrayList<>()).addIfAbsent(sessionId);
        sessionOwners.put(sessionId, userId);
        mirrorSessionAdd(userId, sessionId);
    }

    public void bindWebSocketSession(long userId, WebSocketSession session) {
        webSocketSessions.put(session.getId(), session);
        bindUserSession(userId, session.getId());
    }

    public void unbindSession(String sessionId) {
        Long userId = sessionOwners.remove(sessionId);
        webSocketSessions.remove(sessionId);
        pendingEvents.remove(sessionId);
        if (userId == null) {
            return;
        }

        CopyOnWriteArrayList<String> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }
        mirrorSessionRemove(userId, sessionId);
    }

    public List<String> findSessions(long userId) {
        Set<String> mirroredSessions = loadSessionsFromRedis(userId);
        if (!mirroredSessions.isEmpty()) {
            return mirroredSessions.stream().sorted().toList();
        }
        return new ArrayList<>(userSessions.getOrDefault(userId, new CopyOnWriteArrayList<>()));
    }

    public List<MessageEventDto> findPendingEvents(String sessionId) {
        return new ArrayList<>(pendingEvents.getOrDefault(sessionId, new CopyOnWriteArrayList<>()));
    }

    public void pushEvent(MessageEventDto event) {
        if (event == null || event.targetUserIds() == null) {
            return;
        }
        for (Long userId : event.targetUserIds()) {
            for (String sessionId : findSessions(userId)) {
                pendingEvents.computeIfAbsent(sessionId, ignored -> new CopyOnWriteArrayList<>()).add(event);
                WebSocketSession session = webSocketSessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(toJson(event)));
                    } catch (IOException ex) {
                        unbindSession(sessionId);
                    }
                }
            }
        }
    }

    private void mirrorSessionAdd(long userId, String sessionId) {
        if (redisTemplate == null) {
            return;
        }
        String key = sessionKey(userId);
        redisTemplate.opsForSet().add(key, sessionId);
        redisTemplate.expire(key, SESSION_TTL);
    }

    private void mirrorSessionRemove(long userId, String sessionId) {
        if (redisTemplate == null) {
            return;
        }
        redisTemplate.opsForSet().remove(sessionKey(userId), sessionId);
    }

    private Set<String> loadSessionsFromRedis(long userId) {
        if (redisTemplate == null) {
            return Collections.emptySet();
        }
        Set<String> sessions = redisTemplate.opsForSet().members(sessionKey(userId));
        return sessions == null ? Collections.emptySet() : sessions;
    }

    private String sessionKey(long userId) {
        return "treehole:dm:online:user:" + userId;
    }

    private String toJson(MessageEventDto event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("failed to serialize message event", ex);
        }
    }
}
