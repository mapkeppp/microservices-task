package com.example.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("song-service", r -> r
                        .path("/songs", "/songs/**")
                        .uri("lb://song-service"))
                .route("resource-service", r -> r
                        .path("/resources", "/resources/**")
                        .uri("lb://resource-service"))
                .build();
    }
}