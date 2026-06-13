package com.fiap.authbuyers.infrastructure.persistence;

import com.fiap.authbuyers.domain.entity.Payment;
import com.fiap.authbuyers.domain.repository.PaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final SpringDataPaymentRepository repo;

    public PaymentRepositoryAdapter(SpringDataPaymentRepository repo) {
        this.repo = repo;
    }

    @Override
    public void add(Payment payment) {
        repo.save(PaymentJpaEntity.fromDomain(payment));
    }

    @Override
    public void update(Payment payment) {
        repo.save(PaymentJpaEntity.fromDomain(payment));
    }

    @Override
    public Optional<Payment> getById(UUID id) {
        return repo.findById(id.toString()).map(PaymentJpaEntity::toDomain);
    }

    @Override
    public Optional<Payment> getByPaymentCode(String paymentCode) {
        return repo.findByPaymentCode(paymentCode).map(PaymentJpaEntity::toDomain);
    }
}
