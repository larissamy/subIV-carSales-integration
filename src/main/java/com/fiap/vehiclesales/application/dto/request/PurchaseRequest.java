package com.fiap.vehiclesales.application.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record PurchaseRequest(
        @NotNull UUID carId,
        @NotNull Instant saleDate
) {}
