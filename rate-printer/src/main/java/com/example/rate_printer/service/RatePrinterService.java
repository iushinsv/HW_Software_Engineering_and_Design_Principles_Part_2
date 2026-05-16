package com.example.rate_printer.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Сервис, который каждые 5 секунд запрашивает курс USDRUB
 * у первого сервиса (currency-rate-provider) и выводит в консоль.
 *
 * @PostConstruct — метод запускается сразу после старта Spring-приложения
 * RestTemplate — делает HTTP GET запрос и парсит JSON в Map
 */
@Service
public class RatePrinterService {

    private static final Logger log = LoggerFactory.getLogger(RatePrinterService.class);
    private static final String RATE_URL = "http://localhost:8080/api/rates/usdrub";

    private final RestTemplate restTemplate;

    // Spring сам подставит RestTemplate (внедрение зависимости через конструктор)
    public RatePrinterService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void startPrinting() {
        // Запускаем в отдельном потоке, чтобы не блокировать старт Spring
        new Thread(() -> {
            while (true) {
                try {
                    // Делаем HTTP GET запрос к Сервису 1
                    // Ответ приходит как Map с ключами "currency" и "rate"
                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = restTemplate.getForObject(RATE_URL, Map.class);

                    log.info("USDRUB: {}", response.get("rate"));
                } catch (Exception e) {
                    log.error("Ошибка при запросе курса: {}", e.getMessage());
                }

                // Ждём 5 секунд
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }
}