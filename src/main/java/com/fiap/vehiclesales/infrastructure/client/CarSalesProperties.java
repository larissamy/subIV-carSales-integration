package com.fiap.vehiclesales.infrastructure.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cars")
public record CarSalesProperties(String serviceUrl) {
}
