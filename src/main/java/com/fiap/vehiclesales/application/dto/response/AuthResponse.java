package com.fiap.vehiclesales.application.dto.response;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        BuyerResponse buyer
) {
}
