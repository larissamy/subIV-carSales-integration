package com.fiap.vehiclesales.domain.entity;

import com.fiap.vehiclesales.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.UUID;

public class Payment {
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RNG = new SecureRandom();

    private final UUID id;
    private PaymentStatus paymentStatus;
    private final String paymentCode;
    private final BigDecimal amount;

    private Payment(UUID id, PaymentStatus paymentStatus, String paymentCode, BigDecimal amount) {
        this.id = Objects.requireNonNull(id);
        this.paymentStatus = Objects.requireNonNull(paymentStatus);
        this.paymentCode = Objects.requireNonNull(paymentCode);
        this.amount = Objects.requireNonNull(amount);
    }

    public static Payment create(BigDecimal amount) {
        return new Payment(
                UUID.randomUUID(),
                PaymentStatus.PENDING,
                generatePaymentCode(),
                amount
        );
    }

    public void confirmPayment() {
        if (paymentStatus == PaymentStatus.PAID) {
            throw new IllegalStateException("Pagamento confirmado.");
        }
        if (paymentStatus == PaymentStatus.CANCELLED) {
            throw new IllegalStateException("Pagamento cancelado.");
        }
        this.paymentStatus = PaymentStatus.PAID;
    }

    public void cancelPayment() {
        if (paymentStatus == PaymentStatus.PAID) {
            throw new IllegalStateException("O pagamento já foi aprovado e não pode ser cancelado.");
        }
        this.paymentStatus = PaymentStatus.CANCELLED;
    }

    private static String generatePaymentCode() {
        int numbers = 100 + RNG.nextInt(900);
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            letters.append(CHARS.charAt(RNG.nextInt(CHARS.length())));
        }
        return numbers + "-" + letters;
    }

    public UUID getId() { return id; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public String getPaymentCode() { return paymentCode; }
    public BigDecimal getAmount() { return amount; }

    void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
}
