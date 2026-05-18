package com.example.currency_rate_provider.model;

/**
 * Модель ответа с курсом валют.
 *
 * Аналог dataclass в Python:
 *   @dataclass
 *   class RateResponse:
 *       currency: str
 *       rate: float
 *
 * Spring Boot (Jackson) автоматически сериализует этот объект в JSON:
 *   {"currency": "USDRUB", "rate": 82.15}
 */
public class RateResponse {

    /** Название валютной пары, например "USDRUB" */
    private String currency;

    /** Текущий курс */
    private double rate;

    // Конструктор без параметров (требуется Jackson для десериализации)
    public RateResponse() {
    }

    // Конструктор со всеми полями
    public RateResponse(String currency, double rate) {
        this.currency = currency;
        this.rate = rate;
    }

    // Геттеры и сеттеры (требуются Jackson для сериализации/десериализации)
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}