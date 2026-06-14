package com.fiap.vehiclesales.infrastructure.persistence;

import com.fiap.vehiclesales.domain.entity.Payment;
import com.fiap.vehiclesales.domain.enums.PaymentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "TB_Payments")
public class PaymentJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(nullable = false, unique = true, length = 20)
    private String paymentCode;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    protected PaymentJpaEntity() {}

    public static PaymentJpaEntity fromDomain(Payment payment) {
        var e = new PaymentJpaEntity();
        e.id = payment.getId().toString();
        e.paymentStatus = payment.getPaymentStatus();
        e.paymentCode = payment.getPaymentCode();
        e.amount = payment.getAmount();
        return e;
    }

    public Payment toDomain() {
        return PaymentMapper.rehydrate(
                UUID.fromString(id),
                paymentStatus,
                paymentCode,
                amount
        );
    }
}
