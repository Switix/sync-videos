package com.switix.gatewayservice.config;

import com.switix.gatewayservice.service.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RefreshScope
@Component
@AllArgsConstructor
public class AuthenticationFilter implements GatewayFilter {


    private RouterValidator validator;
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (validator.isSecured.test(request)) {
            if (authMissing(request)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "MISSING_TOKEN");
            }

            String authorizationHeader = request.getHeaders().getFirst("Authorization");

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_AUTHORIZATION_HEADER");
            }

            String token = authorizationHeader.substring(7);

            if (jwtUtil.isExpired(token)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "EXPIRED_ACCESS_TOKEN");
            }
        }
        return chain.filter(exchange);
    }

    private boolean authMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }

}