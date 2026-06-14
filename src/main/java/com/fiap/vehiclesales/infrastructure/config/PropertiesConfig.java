package com.fiap.vehiclesales.infrastructure.config;

import com.fiap.vehiclesales.infrastructure.client.CarSalesProperties;
import com.fiap.vehiclesales.infrastructure.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, CarSalesProperties.class})
public class PropertiesConfig {
}
