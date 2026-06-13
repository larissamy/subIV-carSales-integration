package com.fiap.authbuyers.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record PurchaseRequest(
        @NotNull UUID carId,
        @NotBlank String buyerCpf,
        @NotNull Instant saleDate
) {}
