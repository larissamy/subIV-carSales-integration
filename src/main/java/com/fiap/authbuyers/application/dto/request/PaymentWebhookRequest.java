package com.fiap.authbuyers.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentWebhookRequest(
        @NotBlank String paymentCode,
        @NotNull Integer status
) {}
