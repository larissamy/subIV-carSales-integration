package com.fiap.vehiclesales.presentation.controller;

import com.fiap.vehiclesales.application.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payments")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @Operation(summary = "Get payment by payment code")
    @GetMapping("/{paymentCode}")
    public ResponseEntity<?> getByPaymentCode(@PathVariable("paymentCode") String paymentCode) {
        return service.getPaymentByPaymentCode(paymentCode)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
