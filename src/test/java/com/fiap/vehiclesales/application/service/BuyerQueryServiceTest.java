package com.fiap.vehiclesales.application.service;

import com.fiap.vehiclesales.domain.entity.Buyer;
import com.fiap.vehiclesales.domain.exception.NotFoundException;
import com.fiap.vehiclesales.domain.repository.BuyerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BuyerQueryServiceTest {

    private BuyerRepository buyerRepository;
    private BuyerQueryService service;

    @BeforeEach
    void setup() {
        buyerRepository = mock(BuyerRepository.class);
        service = new BuyerQueryService(buyerRepository);
    }

    @Test
    void shouldFindAllBuyers() {
        Buyer buyer = buyer("Larissa", "larissa@example.com", "12345678901");
        when(buyerRepository.findAll()).thenReturn(List.of(buyer));

        var response = service.findAll();

        assertEquals(1, response.size());
        assertEquals("larissa@example.com", response.get(0).email());
        assertEquals("12345678901", response.get(0).cpf());
    }

    @Test
    void shouldFindBuyerById() {
        Buyer buyer = buyer("Larissa", "larissa@example.com", "12345678901");
        when(buyerRepository.findById(buyer.getId())).thenReturn(Optional.of(buyer));

        var response = service.findById(buyer.getId());

        assertEquals(buyer.getId(), response.id());
        assertEquals("Larissa", response.name());
    }

    @Test
    void shouldThrowWhenBuyerIdDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(buyerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findById(id));
    }

    private Buyer buyer(String name, String email, String cpf) {
        Buyer buyer = new Buyer();
        buyer.setName(name);
        buyer.setEmail(email);
        buyer.setCpf(cpf);
        buyer.setPasswordHash("hash");
        buyer.prePersist();
        return buyer;
    }
}
