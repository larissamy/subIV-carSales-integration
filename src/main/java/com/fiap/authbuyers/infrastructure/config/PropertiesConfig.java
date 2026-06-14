package com.fiap.authbuyers.infrastructure.config;

import com.fiap.authbuyers.infrastructure.client.CarSalesProperties;
import com.fiap.authbuyers.infrastructure.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, CarSalesProperties.class})
public class PropertiesConfig {
}
