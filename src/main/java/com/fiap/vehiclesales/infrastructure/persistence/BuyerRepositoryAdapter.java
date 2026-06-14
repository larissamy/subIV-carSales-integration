package com.fiap.vehiclesales.infrastructure.persistence;

import com.fiap.vehiclesales.domain.entity.Buyer;
import com.fiap.vehiclesales.domain.repository.BuyerRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BuyerRepositoryAdapter implements BuyerRepository {

    private final SpringDataBuyerRepository repository;

    public BuyerRepositoryAdapter(SpringDataBuyerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Buyer save(Buyer buyer) {
        return repository.save(buyer);
    }

    @Override
    public Optional<Buyer> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public Optional<Buyer> findByCpf(String cpf) {
        return repository.findByCpf(cpf);
    }

    @Override
    public Optional<Buyer> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public List<Buyer> findAll() {
        return repository.findAll();
    }
}
