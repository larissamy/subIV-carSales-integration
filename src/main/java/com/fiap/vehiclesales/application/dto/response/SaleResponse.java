package com.fiap.vehiclesales.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SaleResponse(
        UUID id,
        UUID carId,
        String buyerCpf,
        BigDecimal price,
        String paymentCode,
        Instant saleDate,
        String paymentStatus
) {}
