package com.hotelbooking.booking.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Конфигурация Resilience4j для retry и circuit breaker
 */
@Configuration
public class ResilienceConfig {

    @Bean
    public Retry confirmAvailabilityRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3) // Максимум 3 попытки
                .waitDuration(Duration.ofMillis(500)) // Задержка 500мс между попытками
                .retryOnException(throwable -> true) // Повторять при любых исключениях
                .build();

        return Retry.of("confirmAvailability", config);
    }
}

