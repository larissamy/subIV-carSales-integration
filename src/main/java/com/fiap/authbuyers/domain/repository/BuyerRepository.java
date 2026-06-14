package com.fiap.authbuyers.domain.repository;

import com.fiap.authbuyers.domain.entity.Buyer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BuyerRepository {
    Buyer save(Buyer buyer);
    Optional<Buyer> findByEmail(String email);
    Optional<Buyer> findById(UUID id);
    List<Buyer> findAll();
}
