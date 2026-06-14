package com.fiap.vehiclesales.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Sale {
    private final UUID id;
    private final UUID carId;
    private UUID paymentId;
    private final String buyerCpf;
    private final BigDecimal price;
    private final Instant createdAt;

    private Sale(UUID id,
                 UUID carId,
                 UUID paymentId,
                 String buyerCpf,
                 BigDecimal price,
                 Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.carId = Objects.requireNonNull(carId);
        this.paymentId = paymentId;
        this.buyerCpf = Objects.requireNonNull(buyerCpf);
        this.price = Objects.requireNonNull(price);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Sale create(UUID carId, String buyerCpf, BigDecimal price, Instant saleDate) {
        Instant ts = saleDate == null ? Instant.now() : saleDate;
        return new Sale(UUID.randomUUID(), carId, null, buyerCpf, price, ts);
    }

    public UUID getId() { return id; }
    public UUID getCarId() { return carId; }
    public UUID getPaymentId() { return paymentId; }
    public String getBuyerCpf() { return buyerCpf; }
    public BigDecimal getPrice() { return price; }
    public Instant getCreatedAt() { return createdAt; }

    public void assignPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }
}
