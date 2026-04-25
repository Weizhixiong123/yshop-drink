package com.clubhub.websocket;

import com.clubhub.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@RequiredArgsConstructor
public class CustomerWebSocketHandler extends TextWebSocketHandler {

    private static final String CUSTOMER_ROLE = "customer";
    private static final String MEMBER_ID_ATTRIBUTE = "memberId";

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final Map<Long, Set<WebSocketSession>> sessionsByMemberId = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long memberId = resolveMemberId(session);
        if (memberId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("未登录"));
            return;
        }

        session.getAttributes().put(MEMBER_ID_ATTRIBUTE, memberId);
        sessionsByMemberId.computeIfAbsent(memberId, ignored -> new CopyOnWriteArraySet<>()).add(session);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                Map.of("type", "CONNECTED", "memberId", memberId))));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        removeSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        removeSession(session);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    public void sendUserDataUpdate(Long memberId) {
        if (memberId == null) {
            return;
        }
        send(memberId, Map.of("type", "USER_DATA_UPDATE"));
    }

    private void send(Long memberId, Object payload) {
        Set<WebSocketSession> sessions = sessionsByMemberId.get(memberId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        String message;
        try {
            message = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("WebSocket 消息序列化失败", e);
        }

        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                removeSession(session);
                continue;
            }
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                removeSession(session);
            }
        }
    }

    private Long resolveMemberId(WebSocketSession session) {
        String token = extractToken(session);
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            String role = jwtUtil.getRole(token);
            if (!CUSTOMER_ROLE.equals(role)) {
                return null;
            }
            return Long.valueOf(jwtUtil.getUserId(token));
        } catch (Exception e) {
            return null;
        }
    }

    private String extractToken(WebSocketSession session) {
        String authorization = session.getHandshakeHeaders().getFirst("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        URI uri = session.getUri();
        if (uri == null || uri.getRawQuery() == null) {
            return null;
        }
        for (String pair : uri.getRawQuery().split("&")) {
            int equalsIndex = pair.indexOf('=');
            if (equalsIndex > 0 && "token".equals(pair.substring(0, equalsIndex))) {
                return pair.substring(equalsIndex + 1);
            }
        }
        return null;
    }

    private void removeSession(WebSocketSession session) {
        Object memberIdAttribute = session.getAttributes().get(MEMBER_ID_ATTRIBUTE);
        if (!(memberIdAttribute instanceof Long memberId)) {
            return;
        }

        Set<WebSocketSession> sessions = sessionsByMemberId.get(memberId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            sessionsByMemberId.remove(memberId, sessions);
        }
    }
}
