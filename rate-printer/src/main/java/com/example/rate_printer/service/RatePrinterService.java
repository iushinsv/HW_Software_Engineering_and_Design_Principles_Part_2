package com.example.rate_printer.service;

import jakarta.annotation.PostConstruct;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class RatePrinterService {

    private static final Logger log = LoggerFactory.getLogger(RatePrinterService.class);

    private final RestTemplate restTemplate;
    private final ServiceDiscovery<Void> serviceDiscovery;
    private final Random random = new Random();

    public RatePrinterService(RestTemplate restTemplate, CuratorFramework client) {
        this.restTemplate = restTemplate;
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(Void.class)
                .client(client)
                .basePath("/services")
                .build();
    }

    @PostConstruct
    public void startPrinting() {
        new Thread(() -> {
            try {
                serviceDiscovery.start();
                log.info("Connected to ZooKeeper, starting rate polling...");
                
                while (true) {
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
                    } catch (Exception e) {
                        log.error("Error during rate polling: {}", e.getMessage());
                        Thread.sleep(5000);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to start service discovery: {}", e.getMessage());
            }
        }).start();
    }
}