package com.fiap.vehiclesales.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        BigDecimal amount,
        String paymentCode,
        String paymentStatus
) {}
