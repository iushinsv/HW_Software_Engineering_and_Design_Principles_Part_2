package com.example.rate_printer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Конфигурация Spring.
 *
 * Создаёт бин RestTemplate — HTTP-клиент, которым
 * RatePrinterService будет делать запросы к Сервису 1.
 *
 * Аналог в Python: import requests
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}