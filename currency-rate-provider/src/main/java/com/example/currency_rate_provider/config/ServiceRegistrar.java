package com.example.currency_rate_provider.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServiceRegistrar {

    private final ServiceDiscovery<Void> serviceDiscovery;

    public ServiceRegistrar(CuratorFramework client, @Value("${server.port}") int port) throws Exception {
        ServiceInstance<Void> instance = ServiceInstance.<Void>builder()
                .name("currency-rate-provider")
                .address("localhost")
                .port(port)
                .uriSpec(new UriSpec("{scheme}://{address}:{port}"))
                .build();

        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(Void.class)
                .client(client)
                .basePath("/services")
                .thisInstance(instance)
                .build();
    }

    @PostConstruct
    public void register() throws Exception {
        serviceDiscovery.start();
        System.out.println("Registered in ZooKeeper as currency-rate-provider");
    }

    @PreDestroy
    public void deregister() throws Exception {
        serviceDiscovery.close();
        System.out.println("Deregistered from ZooKeeper");
    }
}