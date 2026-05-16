package com.example.rate_printer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация observability для rate-printer.
 *
 * Включает:
 * 1. Логирование версии при старте
 * 2. ClientNameInterceptor — добавляет заголовок X-Service-Name в исходящие запросы
 *    (зарегистрирован в RestTemplateConfig)
 */
@Configuration
public class ObservabilityConfig {

    private static final Logger log = LoggerFactory.getLogger(ObservabilityConfig.class);

    /**
     * ApplicationRunner: выводит версию приложения в лог при старте.
     */
    @Bean
    public ApplicationRunner logVersionOnStartup(
            @Value("${info.app.version:unknown}") String appVersion,
            @Value("${spring.application.name:unknown}") String appName) {
        return args -> log.info("=== {} v{} started ===", appName, appVersion);
    }
}