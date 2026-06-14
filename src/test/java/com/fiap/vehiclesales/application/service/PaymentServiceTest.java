package com.fiap.vehiclesales.application.service;

import com.fiap.vehiclesales.application.dto.request.PaymentWebhookRequest;
import com.fiap.vehiclesales.domain.entity.Payment;
import com.fiap.vehiclesales.domain.entity.Sale;
import com.fiap.vehiclesales.domain.exception.NotFoundException;
import com.fiap.vehiclesales.domain.repository.PaymentRepository;
import com.fiap.vehiclesales.domain.repository.SaleRepository;
import com.fiap.vehiclesales.infrastructure.client.CarServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private PaymentRepository paymentRepository;
    private SaleRepository saleRepository;
    private CarServiceClient carServiceClient;
    private PaymentService service;

    @BeforeEach
    void setup() {
        paymentRepository = mock(PaymentRepository.class);
        saleRepository = mock(SaleRepository.class);
        carServiceClient = mock(CarServiceClient.class);
        service = new PaymentService(paymentRepository, saleRepository, carServiceClient);
    }

    @Test
    void shouldConfirmPaymentAndMarkCarAsSold() {
        var payment = Payment.create(BigDecimal.valueOf(100000));
        var sale = Sale.create(UUID.randomUUID(), "12345678901", BigDecimal.valueOf(100000), Instant.now());
        sale.assignPaymentId(payment.getId());
        when(paymentRepository.getByPaymentCode(payment.getPaymentCode())).thenReturn(Optional.of(payment));
        when(saleRepository.getByPaymentId(payment.getId())).thenReturn(Optional.of(sale));

        service.confirm(new PaymentWebhookRequest(payment.getPaymentCode(), 1));

        assertEquals("PAID", payment.getPaymentStatus().name());
        verify(paymentRepository).update(payment);
        verify(carServiceClient).markCarAsSold(sale.getCarId());
    }

    @Test
    void shouldCancelPaymentAndMakeCarAvailable() {
        var payment = Payment.create(BigDecimal.valueOf(100000));
        var sale = Sale.create(UUID.randomUUID(), "12345678901", BigDecimal.valueOf(100000), Instant.now());
        sale.assignPaymentId(payment.getId());
        when(paymentRepository.getByPaymentCode(payment.getPaymentCode())).thenReturn(Optional.of(payment));
        when(saleRepository.getByPaymentId(payment.getId())).thenReturn(Optional.of(sale));

        service.confirm(new PaymentWebhookRequest(payment.getPaymentCode(), 2));

        assertEquals("CANCELLED", payment.getPaymentStatus().name());
        verify(carServiceClient).makeCarAvailable(sale.getCarId());
    }

    @Test
    void shouldIgnorePendingWebhookForCarStatus() {
        var payment = Payment.create(BigDecimal.valueOf(100000));
        var sale = Sale.create(UUID.randomUUID(), "12345678901", BigDecimal.valueOf(100000), Instant.now());
        sale.assignPaymentId(payment.getId());
        when(paymentRepository.getByPaymentCode(payment.getPaymentCode())).thenReturn(Optional.of(payment));
        when(saleRepository.getByPaymentId(payment.getId())).thenReturn(Optional.of(sale));

        service.confirm(new PaymentWebhookRequest(payment.getPaymentCode(), 0));

        assertEquals("PENDING", payment.getPaymentStatus().name());
        verify(carServiceClient, never()).markCarAsSold(any());
        verify(carServiceClient, never()).makeCarAvailable(any());
    }


    @Test
    void shouldReturnPaymentByPaymentCode() {
        var payment = Payment.create(BigDecimal.valueOf(100000));
        when(paymentRepository.getByPaymentCode(payment.getPaymentCode())).thenReturn(Optional.of(payment));

        var response = service.getPaymentByPaymentCode(payment.getPaymentCode());

        assertTrue(response.isPresent());
        assertEquals(payment.getId(), response.get().id());
        assertEquals(payment.getAmount(), response.get().amount());
        assertEquals(payment.getPaymentCode(), response.get().paymentCode());
        assertEquals("PENDING", response.get().paymentStatus());
    }

    @Test
    void shouldReturnEmptyWhenPaymentCodeDoesNotExist() {
        when(paymentRepository.getByPaymentCode("404-ABC")).thenReturn(Optional.empty());

        var response = service.getPaymentByPaymentCode("404-ABC");

        assertTrue(response.isEmpty());
    }

    @Test
    void shouldThrowWhenWebhookStatusIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.confirm(new PaymentWebhookRequest("123-ABC", null)));
    }

    @Test
    void shouldThrowWhenSaleDoesNotExistForPayment() {
        var payment = Payment.create(BigDecimal.valueOf(100000));
        when(paymentRepository.getByPaymentCode(payment.getPaymentCode())).thenReturn(Optional.of(payment));
        when(saleRepository.getByPaymentId(payment.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.confirm(new PaymentWebhookRequest(payment.getPaymentCode(), 1)));
        verify(paymentRepository).update(payment);
    }

    @Test
    void shouldThrowWhenPaymentDoesNotExist() {
        when(paymentRepository.getByPaymentCode("404-ABC")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.confirm(new PaymentWebhookRequest("404-ABC", 1)));
    }

    @Test
    void shouldThrowForInvalidPaymentStatus() {
        assertThrows(IllegalArgumentException.class, () -> service.confirm(new PaymentWebhookRequest("123-ABC", 9)));
    }
}
