package com.fiap.authbuyers.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CarResponse(
        UUID id,
        String brand,
        String model,
        int year,
        String color,
        String licensePlate,
        BigDecimal price,
        String status,
        Instant updatedAt
) {}
