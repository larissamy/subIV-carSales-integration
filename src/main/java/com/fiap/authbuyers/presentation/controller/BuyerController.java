package com.fiap.authbuyers.presentation.controller;

import com.fiap.authbuyers.application.dto.response.BuyerResponse;
import com.fiap.authbuyers.application.service.BuyerQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/buyers")
@Tag(name = "Buyers")
public class BuyerController {

    private final BuyerQueryService buyerQueryService;

    public BuyerController(BuyerQueryService buyerQueryService) {
        this.buyerQueryService = buyerQueryService;
    }

    @GetMapping
    @Operation(summary = "List all buyers")
    public ResponseEntity<List<BuyerResponse>> findAll() {
        return ResponseEntity.ok(buyerQueryService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get buyer by id")
    public ResponseEntity<BuyerResponse> findById(
            @Parameter(description = "Buyer ID", required = true)
            @PathVariable("id") UUID id) {
        return ResponseEntity.ok(buyerQueryService.findById(id));
    }
}
