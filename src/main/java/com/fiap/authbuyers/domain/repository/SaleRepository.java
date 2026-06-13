package com.fiap.authbuyers.domain.repository;

import com.fiap.authbuyers.domain.entity.Sale;

import java.util.Optional;
import java.util.UUID;

public interface SaleRepository {
    void add(Sale sale);
    void update(Sale sale);
    Optional<Sale> getById(UUID id);
    Optional<Sale> getByPaymentId(UUID paymentId);
}
