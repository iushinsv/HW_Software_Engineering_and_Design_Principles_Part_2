package com.example.rate_printer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Pact consumer test для rate-printer (Pact V4 HTTP DSL).
 * Описывает контракт: при GET /api/rates/usdrub ожидаем JSON
 * с полями "currency" (String) и "rate" (Number).
 *
 * Используется новый V4 DSL (expectsToReceiveHttpInteraction),
 * совместимый с Pact 4.6.x.
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "currency-rate-provider", port = "0")
public class RatePrinterPactTest {

    @Pact(consumer = "rate-printer")
    public V4Pact createPact(PactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody()
                .stringType("currency", "USDRUB")
                .numberType("rate", 80.0);
        return builder
            .expectsToReceiveHttpInteraction("a request for USDRUB rate", http -> http
                .withRequest(request -> request
                    .path("/api/rates/usdrub")
                    .method("GET")
                )
                .willRespondWith(response -> response
                    .status(200)
                    .headers(Map.of("Content-Type", "application/json"))
                    .body(body)
                )
            )
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPact")
    public void testGetUsdRub(MockServer mockServer) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> response = restTemplate.getForObject(
                mockServer.getUrl() + "/api/rates/usdrub", Map.class);

        assertNotNull(response);
        assertEquals("USDRUB", response.get("currency"));
        assertNotNull(response.get("rate"));
    }
}