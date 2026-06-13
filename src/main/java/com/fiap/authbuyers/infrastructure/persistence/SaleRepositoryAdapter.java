package com.fiap.authbuyers.infrastructure.persistence;

import com.fiap.authbuyers.domain.entity.Sale;
import com.fiap.authbuyers.domain.repository.SaleRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class SaleRepositoryAdapter implements SaleRepository {

    private final SpringDataSaleRepository repo;

    public SaleRepositoryAdapter(SpringDataSaleRepository repo) {
        this.repo = repo;
    }

    @Override
    public void add(Sale sale) {
        repo.save(SaleJpaEntity.fromDomain(sale));
    }

    @Override
    public void update(Sale sale) {
        repo.save(SaleJpaEntity.fromDomain(sale));
    }

    @Override
    public Optional<Sale> getById(UUID id) {
        return repo.findById(id.toString()).map(SaleJpaEntity::toDomain);
    }

    @Override
    public Optional<Sale> getByPaymentId(UUID paymentId) {
        return repo.findByPaymentId(paymentId.toString()).map(SaleJpaEntity::toDomain);
    }
}
