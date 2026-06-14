package com.fiap.vehiclesales.presentation.controller;

import com.fiap.vehiclesales.application.dto.request.PaymentWebhookRequest;
import com.fiap.vehiclesales.application.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payment Webhooks")
@RestController
@RequestMapping("/api/webhooks")
public class WebhooksController {

    private final PaymentService service;

    public WebhooksController(PaymentService service) {
        this.service = service;
    }

    @Operation(summary = "Payment processor webhook. Status: 0=PENDING, 1=PAID, 2=CANCELLED")
    @PostMapping("/payments")
    public ResponseEntity<?> confirm(@Valid @RequestBody PaymentWebhookRequest request) {
        service.confirm(request);
        return ResponseEntity.noContent().build();
    }
}
