package com.fiap.vehiclesales.infrastructure.persistence;

import com.fiap.vehiclesales.domain.entity.Sale;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

final class SaleMapper {
    private SaleMapper() {}

    static Sale rehydrate(UUID id, UUID carId, UUID paymentId, String buyerCpf, BigDecimal price, Instant createdAt) {
        try {
            Constructor<Sale> c = Sale.class.getDeclaredConstructor(UUID.class, UUID.class, UUID.class, String.class, BigDecimal.class, Instant.class);
            c.setAccessible(true);
            return c.newInstance(id, carId, paymentId, buyerCpf, price, createdAt);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to rehydrate Sale", e);
        }
    }
}
