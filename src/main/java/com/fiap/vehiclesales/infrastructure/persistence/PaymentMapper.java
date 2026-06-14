package com.fiap.vehiclesales.infrastructure.persistence;

import com.fiap.vehiclesales.domain.entity.Payment;
import com.fiap.vehiclesales.domain.enums.PaymentStatus;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.UUID;

final class PaymentMapper {
    private PaymentMapper() {}

    static Payment rehydrate(UUID id, PaymentStatus status, String paymentCode, BigDecimal amount) {
        try {
            Constructor<Payment> c = Payment.class.getDeclaredConstructor(UUID.class, PaymentStatus.class, String.class, BigDecimal.class);
            c.setAccessible(true);
            return c.newInstance(id, status, paymentCode, amount);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to rehydrate Payment", e);
        }
    }
}
