package com.fiap.authbuyers.domain.repository;

import com.fiap.authbuyers.domain.entity.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    void add(Payment payment);
    void update(Payment payment);
    Optional<Payment> getById(UUID id);
    Optional<Payment> getByPaymentCode(String paymentCode);
}
