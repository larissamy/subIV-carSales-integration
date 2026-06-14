package com.fiap.vehiclesales.domain.entity;

import com.fiap.vehiclesales.domain.enums.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainEntityTest {

    @Test
    void shouldCreatePaymentWithPendingStatusAndGeneratedCode() {
        Payment payment = Payment.create(BigDecimal.valueOf(99999));

        assertNotNull(payment.getId());
        assertEquals(PaymentStatus.PENDING, payment.getPaymentStatus());
        assertEquals(BigDecimal.valueOf(99999), payment.getAmount());
        assertTrue(payment.getPaymentCode().matches("\\d{3}-[A-Z]{3}"));
    }

    @Test
    void shouldConfirmPendingPayment() {
        Payment payment = Payment.create(BigDecimal.valueOf(100));

        payment.confirmPayment();

        assertEquals(PaymentStatus.PAID, payment.getPaymentStatus());
    }

    @Test
    void shouldNotConfirmPaymentTwice() {
        Payment payment = Payment.create(BigDecimal.valueOf(100));
        payment.confirmPayment();

        assertThrows(IllegalStateException.class, payment::confirmPayment);
    }

    @Test
    void shouldNotConfirmCancelledPayment() {
        Payment payment = Payment.create(BigDecimal.valueOf(100));
        payment.cancelPayment();

        assertThrows(IllegalStateException.class, payment::confirmPayment);
    }

    @Test
    void shouldCancelPendingPayment() {
        Payment payment = Payment.create(BigDecimal.valueOf(100));

        payment.cancelPayment();

        assertEquals(PaymentStatus.CANCELLED, payment.getPaymentStatus());
    }

    @Test
    void shouldNotCancelPaidPayment() {
        Payment payment = Payment.create(BigDecimal.valueOf(100));
        payment.confirmPayment();

        assertThrows(IllegalStateException.class, payment::cancelPayment);
    }

    @Test
    void shouldCreateSaleWithProvidedSaleDate() {
        UUID carId = UUID.randomUUID();
        Instant saleDate = Instant.parse("2026-06-12T10:00:00Z");

        Sale sale = Sale.create(carId, "12345678901", BigDecimal.valueOf(120000), saleDate);

        assertNotNull(sale.getId());
        assertEquals(carId, sale.getCarId());
        assertEquals("12345678901", sale.getBuyerCpf());
        assertEquals(BigDecimal.valueOf(120000), sale.getPrice());
        assertEquals(saleDate, sale.getCreatedAt());
    }

    @Test
    void shouldCreateSaleWithCurrentDateWhenSaleDateIsNull() {
        Sale sale = Sale.create(UUID.randomUUID(), "12345678901", BigDecimal.valueOf(120000), null);

        assertNotNull(sale.getCreatedAt());
    }

    @Test
    void shouldAssignPaymentIdToSale() {
        Sale sale = Sale.create(UUID.randomUUID(), "12345678901", BigDecimal.valueOf(120000), Instant.now());
        UUID paymentId = UUID.randomUUID();

        sale.assignPaymentId(paymentId);

        assertEquals(paymentId, sale.getPaymentId());
    }

    @Test
    void shouldNormalizeBuyerEmailAndCpfOnPersistAndUpdate() {
        Buyer buyer = new Buyer();
        buyer.setName("Larissa");
        buyer.setEmail(" LARISSA.TEST@EXAMPLE.COM ");
        buyer.setCpf("123.456.789-01");
        buyer.setPasswordHash("hash");

        buyer.prePersist();

        assertNotNull(buyer.getId());
        assertEquals("larissa.test@example.com", buyer.getEmail());
        assertEquals("12345678901", buyer.getCpf());
        assertNotNull(buyer.getCreatedAt());
        assertNotNull(buyer.getUpdatedAt());

        buyer.setEmail(" NOVO.EMAIL@EXAMPLE.COM ");
        buyer.setCpf("987.654.321-00");
        buyer.preUpdate();

        assertEquals("novo.email@example.com", buyer.getEmail());
        assertEquals("98765432100", buyer.getCpf());
        assertNotNull(buyer.getUpdatedAt());
    }

    @Test
    void shouldKeepNullEmailAndCpfWhenPersistingBuyerWithNullValues() {
        Buyer buyer = new Buyer();
        buyer.setName("Larissa");
        buyer.setPasswordHash("hash");

        buyer.prePersist();
        buyer.preUpdate();

        assertNotNull(buyer.getId());
        assertNull(buyer.getEmail());
        assertNull(buyer.getCpf());
    }
}
