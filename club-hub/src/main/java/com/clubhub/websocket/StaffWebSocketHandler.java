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

@Component
@RequiredArgsConstructor
public class StaffWebSocketHandler extends TextWebSocketHandler {

    private static final String STAFF_ID_ATTRIBUTE = "staffId";

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        StaffIdentity identity = resolveIdentity(session);
        if (identity == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("未登录"));
            return;
        }

        session.getAttributes().put(STAFF_ID_ATTRIBUTE, identity.staffId());
        sessions.add(session);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                Map.of("type", "CONNECTED", "staffId", identity.staffId(), "role", identity.role()))));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        sessions.remove(session);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    public void sendMemberDataUpdate(Long memberId) {
        if (memberId == null) {
            return;
        }
        send(Map.of("type", "MEMBER_DATA_UPDATE", "memberId", memberId));
    }

    private void send(Object payload) {
        if (sessions.isEmpty()) {
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
                sessions.remove(session);
                continue;
            }
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                sessions.remove(session);
            }
        }
    }

    private StaffIdentity resolveIdentity(WebSocketSession session) {
        String token = extractToken(session);
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            String role = jwtUtil.getRole(token);
            if (!"staff".equals(role) && !"manager".equals(role) && !"owner".equals(role)) {
                return null;
            }
            return new StaffIdentity(jwtUtil.getUserId(token), role);
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

    private record StaffIdentity(String staffId, String role) {
    }
}
