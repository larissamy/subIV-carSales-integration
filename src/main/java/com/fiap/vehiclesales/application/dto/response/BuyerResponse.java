package com.fiap.vehiclesales.application.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BuyerResponse(
        UUID id,
        String name,
        String email,
        String cpf,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
