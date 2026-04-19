package com.clubhub.config.interceptor;

import com.clubhub.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录\"}");
            return false;
        }

        try {
            token = token.substring(7);
            Long userId = jwtUtil.getUserId(token);
            String role = jwtUtil.getRole(token);

            request.setAttribute("userId", userId);
            request.setAttribute("role", role);

            // 权限校验：客户只能访问 /api/customer/**
            String uri = request.getRequestURI();
            if ("customer".equals(role) && !uri.startsWith("/api/customer/")) {
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"msg\":\"无权限\"}");
                return false;
            }

            // 店员不能访问 /api/owner/**
            if ("staff".equals(role) && uri.startsWith("/api/owner/")) {
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"msg\":\"无权限\"}");
                return false;
            }

            return true;
        } catch (Exception e) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"登录已过期\"}");
            return false;
        }
    }
}
