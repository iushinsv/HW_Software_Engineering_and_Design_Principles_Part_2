package com.example.currency_rate_provider.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.io.IOException;

/**
 * Конфигурация observability для currency-rate-provider.
 *
 * Включает:
 * 1. Логирование версии при старте
 * 2. CommonsRequestLoggingFilter — логирование входящих HTTP запросов/ответов
 * 3. Кастомный фильтр для метрики количества запросов с разбивкой по клиентам
 */
@Configuration
public class ObservabilityConfig {

    private static final Logger log = LoggerFactory.getLogger(ObservabilityConfig.class);

    /**
     * ApplicationRunner: выводит версию приложения в лог при старте.
     * Значение берётся из application.properties: info.app.version
     */
    @Bean
    public ApplicationRunner logVersionOnStartup(
            @Value("${info.app.version:unknown}") String appVersion,
            @Value("${spring.application.name:unknown}") String appName) {
        return args -> log.info("=== {} v{} started ===", appName, appVersion);
    }

    /**
     * CommonsRequestLoggingFilter: логирует каждый входящий HTTP-запрос.
     * - beforeRequest: URI, метод, заголовки
     * - afterRequest: статус ответа
     *
     * Это избавляет от необходимости писать логи вручную в каждом контроллере.
     */
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter() {
            @Override
            protected void afterRequest(HttpServletRequest request, String message) {
                // message уже содержит URI, метод, заголовки
                log.info("[REQUEST] {}", message);
            }
        };
        // Настройка: логировать URI, метод, заголовки, query string, payload
        filter.setIncludeQueryString(true);
        filter.setIncludeHeaders(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setAfterMessagePrefix("");
        return filter;
    }

    /**
     * Кастомный фильтр для метрики "http.server.requests.by.client".
     *
     * Извлекает заголовок X-Service-Name (который добавляет клиент rate-printer),
     * и инкрементит счётчик Micrometer с тегом client.
     * Это позволяет в Grafana построить график "RPS по клиентам".
     */
    @Bean
    public Filter clientMetricsFilter(MeterRegistry meterRegistry) {
        return new Filter() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest servletRequest,
                                 jakarta.servlet.ServletResponse servletResponse,
                                 FilterChain chain) throws IOException, ServletException {

                HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

                // Извлекаем имя клиента из заголовка X-Service-Name
                // Клиент rate-printer отправляет этот заголовок
                String clientName = httpRequest.getHeader("X-Service-Name");
                if (clientName == null || clientName.isBlank()) {
                    clientName = "unknown";
                }

                // Увеличиваем счётчик запросов для данного клиента
                Counter.builder("http.server.requests.by.client")
                        .tag("client", clientName)
                        .tag("method", httpRequest.getMethod())
                        .tag("uri", httpRequest.getRequestURI())
                        .register(meterRegistry)
                        .increment();

                chain.doFilter(servletRequest, servletResponse);
            }
        };
    }
}