package com.hotelbooking.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация маршрутизации API Gateway
 */
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
                                         @Value("${jwt.secret:mySuperSecretKeyForJWTTokenSigningInHotelBookingSystem2024}") String jwtSecret) {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtSecret);

        return builder.routes()
            // Маршруты для Booking Service
            .route("booking-service", r -> r
                .path("/api/bookings/**", "/api/booking/**", "/api/user/**")
                .filters(f -> f
                    .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                .uri("lb://booking-service"))

            // Маршруты для Hotel Service
            .route("hotel-service", r -> r
                .path("/api/hotels/**", "/api/rooms/**")
                .filters(f -> f
                    .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                .uri("lb://hotel-service"))

            .build();
    }
}
