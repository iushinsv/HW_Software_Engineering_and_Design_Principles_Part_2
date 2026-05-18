package com.example.currency_rate_provider.controller;

import com.example.currency_rate_provider.model.RateResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 * REST-контроллер, который обрабатывает HTTP-запросы на /api/rates/usdrub.
 *
 * Аналог в Python (Flask):
 *   @app.route('/api/rates/usdrub')
 *   def get_usdrub():
 *       rate = 80.0 + random.uniform(-2, 2)
 *       return {"currency": "USDRUB", "rate": rate}
 *
 * @RestController — говорит Spring, что этот класс обрабатывает HTTP-запросы
 *                  и автоматически сериализует ответы в JSON
 * @RequestMapping("/api/rates") — все endpoint'ы в этом классе будут начинаться с /api/rates
 * @GetMapping("/usdrub") — метод handler для GET-запроса на /api/rates/usdrub
 */
@RestController
@RequestMapping("/api/rates")
public class RateController {

    private static final double BASE_RATE = 80.0;   // Базовый курс USDRUB
    private static final double VARIATION = 2.0;    // Разброс ±2 рубля
    private final Random random = new Random();     // Генератор случайных чисел

    /**
     * Обрабатывает GET /api/rates/usdrub.
     * Возвращает текущий курс USDRUB = базовое значение + случайное отклонение.
     */
    @GetMapping("/usdrub")
    public RateResponse getUsdRub() {
        // Генерируем случайное число от -VARIATION до +VARIATION и прибавляем к базовому курсу
        double rate = BASE_RATE + (random.nextDouble() * 2 * VARIATION - VARIATION);
        // Округляем до 2 знаков после запятой (как реальные курсы валют)
        rate = Math.round(rate * 100.0) / 100.0;

        return new RateResponse("USDRUB", rate);
    }
}