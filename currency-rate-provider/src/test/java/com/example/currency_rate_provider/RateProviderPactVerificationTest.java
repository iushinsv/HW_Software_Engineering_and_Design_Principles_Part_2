package com.example.currency_rate_provider;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import com.example.currency_rate_provider.config.ServiceRegistrar;
import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Pact provider verification test для currency-rate-provider.
 * Запускает Spring Boot приложение на случайном порту, затем
 * загружает контракты из Pact Broker и верифицирует их.
 * <p>
 * CuratorFramework замокан, чтобы не требовать реального ZooKeeper.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@Provider("currency-rate-provider")
@PactBroker(url = "http://localhost:9292")
public class RateProviderPactVerificationTest {

    @LocalServerPort
    private int port;

    @MockBean
    private CuratorFramework curatorFramework;

    @MockBean
    private ServiceRegistrar serviceRegistrar;

    @BeforeEach
    public void setup(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    public void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }
}