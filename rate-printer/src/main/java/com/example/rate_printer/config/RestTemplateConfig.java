package com.example.rate_printer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * Конфигурация RestTemplate.
 *
 * Добавляет ClientNameInterceptor, который:
 * 1. Добавляет заголовок X-Service-Name: rate-printer во все исходящие запросы.
 *    На сервере (currency-rate-provider) этот заголовок извлекается фильтром
 *    ClientMetricsFilter для создания метрики с разбивкой по клиентам.
 * 2. Логирует исходящий запрос (метод, URI, заголовки).
 * 3. Логирует входящий ответ (статус).
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // Добавляем interceptor в цепочку
        restTemplate.getInterceptors().add(new ClientRequestLoggingInterceptor());
        return restTemplate;
    }

    /**
     * Interceptor для RestTemplate.
     * - Перед отправкой: логирует запрос + добавляет заголовок X-Service-Name
     * - После ответа: логирует статус ответа
     */
    private static class ClientRequestLoggingInterceptor implements ClientHttpRequestInterceptor {

        private static final org.slf4j.Logger log =
                org.slf4j.LoggerFactory.getLogger(ClientRequestLoggingInterceptor.class);

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                            ClientHttpRequestExecution execution) throws IOException {

            // Добавляем заголовок с именем клиента — для метрики на сервере
            request.getHeaders().add("X-Service-Name", "rate-printer");

            // Логируем исходящий запрос (ТЗ: "На клиенте — залогать запрос")
            log.info("[CLIENT REQUEST] {} {}", request.getMethod(), request.getURI());

            // Выполняем запрос
            ClientHttpResponse response = execution.execute(request, body);

            // Логируем ответ (ТЗ: "На клиенте — залогать ответ")
            log.info("[CLIENT RESPONSE] {} {} -> {}",
                    request.getMethod(), request.getURI(), response.getStatusCode());

            return response;
        }
    }
}