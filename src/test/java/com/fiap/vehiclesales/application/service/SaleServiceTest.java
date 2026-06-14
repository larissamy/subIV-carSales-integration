package com.fiap.vehiclesales.application.service;

import com.fiap.vehiclesales.application.dto.request.PurchaseRequest;
import com.fiap.vehiclesales.application.dto.response.CarResponse;
import com.fiap.vehiclesales.domain.entity.Buyer;
import com.fiap.vehiclesales.domain.entity.Payment;
import com.fiap.vehiclesales.domain.entity.Sale;
import com.fiap.vehiclesales.domain.exception.BusinessException;
import com.fiap.vehiclesales.domain.repository.BuyerRepository;
import com.fiap.vehiclesales.domain.repository.PaymentRepository;
import com.fiap.vehiclesales.domain.repository.SaleRepository;
import com.fiap.vehiclesales.infrastructure.client.CarServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SaleServiceTest {

    private CarServiceClient carServiceClient;
    private SaleRepository saleRepository;
    private PaymentRepository paymentRepository;
    private BuyerRepository buyerRepository;
    private SaleService service;

    @BeforeEach
    void setup() {
        carServiceClient = mock(CarServiceClient.class);
        saleRepository = mock(SaleRepository.class);
        paymentRepository = mock(PaymentRepository.class);
        buyerRepository = mock(BuyerRepository.class);
        service = new SaleService(carServiceClient, saleRepository, paymentRepository, buyerRepository);
    }

    @Test
    void shouldListAvailableCars() {
        var car = car("AVAILABLE");
        when(carServiceClient.getAvailableCars()).thenReturn(List.of(car));

        var response = service.getAvailableCars();

        assertEquals(1, response.size());
        assertEquals("AVAILABLE", response.get(0).status());
    }

    @Test
    void shouldListSoldCars() {
        var car = car("SOLD");
        when(carServiceClient.getSoldCars()).thenReturn(List.of(car));

        var response = service.getSoldCars();

        assertEquals(1, response.size());
        assertEquals("SOLD", response.get(0).status());
    }

    @Test
    void shouldPurchaseAvailableCarUsingAuthenticatedBuyerCpf() {
        var car = car("AVAILABLE");
        var buyer = buyer();
        var request = new PurchaseRequest(car.id(), Instant.now());

        when(buyerRepository.findByEmail(buyer.getEmail())).thenReturn(Optional.of(buyer));
        when(carServiceClient.getCarById(car.id())).thenReturn(car);

        var response = service.purchase(request, buyer.getEmail());

        assertEquals(car.id(), response.carId());
        assertEquals("12345678901", response.buyerCpf());
        assertEquals("PENDING", response.paymentStatus());
        assertNotNull(response.paymentCode());
        verify(carServiceClient).reserveCar(car.id());
        verify(paymentRepository).add(any(Payment.class));
        verify(saleRepository).add(any(Sale.class));
    }

    @Test
    void shouldNotPurchaseUnavailableCar() {
        var car = car("RESERVED");
        var buyer = buyer();
        var request = new PurchaseRequest(car.id(), Instant.now());

        when(buyerRepository.findByEmail(buyer.getEmail())).thenReturn(Optional.of(buyer));
        when(carServiceClient.getCarById(car.id())).thenReturn(car);

        assertThrows(BusinessException.class, () -> service.purchase(request, buyer.getEmail()));
        verify(carServiceClient, never()).reserveCar(any());
    }

    private Buyer buyer() {
        Buyer buyer = new Buyer();
        buyer.setName("Larissa Test");
        buyer.setEmail("larissa.test@example.com");
        buyer.setCpf("12345678901");
        buyer.setPasswordHash("hash");
        buyer.prePersist();
        return buyer;
    }

    private CarResponse car(String status) {
        return new CarResponse(
                UUID.randomUUID(),
                "Toyota",
                "Corolla",
                2023,
                "Prata",
                "ABC1D23",
                BigDecimal.valueOf(120000),
                status,
                Instant.now()
        );
    }
}
