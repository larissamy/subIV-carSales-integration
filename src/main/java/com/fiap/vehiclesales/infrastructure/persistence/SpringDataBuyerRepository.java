package com.fiap.vehiclesales.infrastructure.persistence;

import com.fiap.vehiclesales.domain.entity.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataBuyerRepository extends JpaRepository<Buyer, UUID> {
    Optional<Buyer> findByEmail(String email);
    Optional<Buyer> findByCpf(String cpf);
}
