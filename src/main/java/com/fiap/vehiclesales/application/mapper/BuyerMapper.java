package com.fiap.vehiclesales.application.mapper;

import com.fiap.vehiclesales.application.dto.response.BuyerResponse;
import com.fiap.vehiclesales.domain.entity.Buyer;

public final class BuyerMapper {

    private BuyerMapper() {
    }

    public static BuyerResponse toResponse(Buyer buyer) {
        return new BuyerResponse(
                buyer.getId(),
                buyer.getName(),
                buyer.getEmail(),
                buyer.getCpf(),
                buyer.getCreatedAt(),
                buyer.getUpdatedAt()
        );
    }
}
