package com.fiap.vehiclesales.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataSaleRepository extends JpaRepository<SaleJpaEntity, String> {
    Optional<SaleJpaEntity> findByPaymentId(String paymentId);
}
