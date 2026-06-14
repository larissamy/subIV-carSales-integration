package com.fiap.authbuyers.infrastructure.persistence;

import com.fiap.authbuyers.domain.entity.Sale;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "TB_Sales")
public class SaleJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(nullable = false, length = 36)
    private String carId;

    @Column(nullable = false, length = 36)
    private String paymentId;

    @Column(nullable = false, length = 14)
    private String buyerCpf;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Instant createdAt;

    protected SaleJpaEntity() {}

    public static SaleJpaEntity fromDomain(Sale sale) {
        var e = new SaleJpaEntity();
        e.id = sale.getId().toString();
        e.carId = sale.getCarId().toString();
        e.paymentId = sale.getPaymentId().toString();
        e.buyerCpf = sale.getBuyerCpf();
        e.price = sale.getPrice();
        e.createdAt = sale.getCreatedAt();
        return e;
    }

    public Sale toDomain() {
        return SaleMapper.rehydrate(
                UUID.fromString(id),
                UUID.fromString(carId),
                UUID.fromString(paymentId),
                buyerCpf,
                price,
                createdAt
        );
    }
}
