package com.switix.gatewayservice.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.function.Predicate;

@Service
public class RouterValidator {

    public static final List<String> openEndpoints = List.of(
            "/auth/register",
            "/auth/login",
            //temporary because it's impossible to set headers to websocket
            "/ws/**"
    );
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    public Predicate<ServerHttpRequest> isSecured = request ->
            openEndpoints.stream()
                    .noneMatch(uri -> antPathMatcher.match(uri, request.getURI().getPath()));

}