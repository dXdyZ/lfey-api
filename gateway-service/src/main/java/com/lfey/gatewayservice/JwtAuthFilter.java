package com.lfey.gatewayservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter {
    private final JwtUtils jwtUtils;

    @Autowired
    public JwtAuthFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        // Пропускаем публичные поинты
        if (path.startsWith("/users/register") ||
            path.startsWith("/users/login") ||
            path.startsWith("/users/confirm/")) {
            return chain.filter(exchange);
        }

        // Извлекаем токен из заголовка
        String token = extractToken(request);
        if (token == null) {
            return unauthorizedResponse(exchange, "Missing or invalid token");
        }

        try {
            if (!jwtUtils.validateToken(token)) {
                return unauthorizedResponse(exchange, "Invalid token");
            }
            // Добавляем данные пользователя в заголовок
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Email", jwtUtils.getUserEmailFromToken(token))
                    .header("X-User-Username", jwtUtils.getUsernameFromToken(token))
                    .header("X-User-Id", jwtUtils.getUserIdFromToken(token))
                    .header("X-User-Role", jwtUtils.getUserRoleFromToken(token))
                    .build();


            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (JwtValidateException ex) {
            return unauthorizedResponse(exchange, ex.getMessage());
        }
    }

    private String extractToken(ServerHttpRequest request) {
        List<String> headers = request.getHeaders().get("Authorization");
        if (headers == null || headers.isEmpty()) return null;
        String header = headers.get(0);
        if (header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String massage) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"error\": \"Unauthorized\", \"message\": \"%s\"}", massage);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}
