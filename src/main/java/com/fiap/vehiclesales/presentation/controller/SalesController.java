package com.fiap.vehiclesales.presentation.controller;

import com.fiap.vehiclesales.application.dto.request.PurchaseRequest;
import com.fiap.vehiclesales.application.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "Vehicle Sales")
@RestController
@RequestMapping("/api/sales")
public class SalesController {

    private final SaleService service;

    public SalesController(SaleService service) {
        this.service = service;
    }

    @Operation(summary = "List available cars ordered by price (asc)")
    @GetMapping("/cars/available")
    public ResponseEntity<?> getAvailableCars() {
        return ResponseEntity.ok(service.getAvailableCars());
    }

    @Operation(summary = "List sold cars ordered by price (asc)")
    @GetMapping("/cars/sold")
    public ResponseEntity<?> getSoldCars() {
        return ResponseEntity.ok(service.getSoldCars());
    }

    @Operation(summary = "Purchase a vehicle and generate a pending payment")
    @PostMapping("/purchase")
    public ResponseEntity<?> purchase(@Valid @RequestBody PurchaseRequest request) {
        var response = service.purchase(request);
        return ResponseEntity.created(URI.create("/api/payments/" + response.paymentCode())).body(response);
    }
}
