package com.example.currency_rate_provider.controller;

import com.example.currency_rate_provider.model.RateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 * REST-контроллер, который обрабатывает HTTP-запросы на /api/rates/usdrub.
 *
 * Конфигурация курса (BASE_RATE, VARIATION) вынесена в application.properties
 * в соответствии с 12-factor app (фактор III — конфигурация в среде выполнения).
 *
 * @RestController — говорит Spring, что этот класс обрабатывает HTTP-запросы
 *                  и автоматически сериализует ответы в JSON
 * @RequestMapping("/api/rates") — все endpoint'ы в этом классе будут начинаться с /api/rates
 * @GetMapping("/usdrub") — метод handler для GET-запроса на /api/rates/usdrub
 */
@RestController
@RequestMapping("/api/rates")
public class RateController {

    private final Random random = new Random();

    @Value("${rate.base}")
    private double baseRate;

    @Value("${rate.variation}")
    private double variation;

    /**
     * Обрабатывает GET /api/rates/usdrub.
     * Возвращает текущий курс USDRUB = базовое значение + случайное отклонение.
     */
    @GetMapping("/usdrub")
    public RateResponse getUsdRub() {
        // Генерируем случайное число от -variation до +variation и прибавляем к базовому курсу
        double rate = baseRate + (random.nextDouble() * 2 * variation - variation);
        // Округляем до 2 знаков после запятой (как реальные курсы валют)
        rate = Math.round(rate * 100.0) / 100.0;

        return new RateResponse("USDRUB", rate);
    }
}
