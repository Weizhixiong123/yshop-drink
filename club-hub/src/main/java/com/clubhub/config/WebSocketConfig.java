package com.clubhub.config;

import com.clubhub.websocket.CustomerWebSocketHandler;
import com.clubhub.websocket.StaffWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final CustomerWebSocketHandler customerWebSocketHandler;
    private final StaffWebSocketHandler staffWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(customerWebSocketHandler, "/ws/customer")
                .setAllowedOriginPatterns("*");
        registry.addHandler(staffWebSocketHandler, "/ws/staff")
                .setAllowedOriginPatterns("*");
    }
}
