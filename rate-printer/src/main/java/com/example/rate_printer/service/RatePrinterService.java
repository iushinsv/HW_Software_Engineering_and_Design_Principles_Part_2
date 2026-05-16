package com.example.rate_printer.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class RatePrinterService {

    private static final Logger log = LoggerFactory.getLogger(RatePrinterService.class);

    private final RestTemplate restTemplate;
    private final ServiceDiscovery<Void> serviceDiscovery;
    private final Random random = new Random();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService executor;

    public RatePrinterService(RestTemplate restTemplate, CuratorFramework client) {
        this.restTemplate = restTemplate;
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(Void.class)
                .client(client)
                .basePath("/services")
                .build();
    }

    @PostConstruct
    public void startPrinting() {
        running.set(true);
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "rate-printer-polling");
            t.setDaemon(false);
            return t;
        });

        executor.submit(() -> {
            try {
                serviceDiscovery.start();
                log.info("Connected to ZooKeeper, starting rate polling...");

                while (running.get()) {
                    try {
                        Collection<ServiceInstance<Void>> instances =
                                serviceDiscovery.queryForInstances("currency-rate-provider");

                        if (instances.isEmpty()) {
                            log.warn("No instances of currency-rate-provider found in ZooKeeper");
                        } else {
                            List<ServiceInstance<Void>> instanceList = new ArrayList<>(instances);
                            ServiceInstance<Void> selected = instanceList.get(random.nextInt(instanceList.size()));

                            String url = "http://" + selected.getAddress() + ":" + selected.getPort() + "/api/rates/usdrub";
                            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

                            log.info("USDRUB: {} (from {}:{})",
                                    response.get("rate"), selected.getAddress(), selected.getPort());
                        }

                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        log.info("Rate polling interrupted, shutting down...");
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        log.error("Error during rate polling: {}", e.getMessage());
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to start service discovery: {}", e.getMessage());
            }
        });
    }

    @PreDestroy
    public void stopPrinting() {
        log.info("Shutting down rate printer gracefully...");
        running.set(false);
        if (executor != null) {
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.warn("Executor did not terminate in time");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("Rate printer stopped");
    }
}
