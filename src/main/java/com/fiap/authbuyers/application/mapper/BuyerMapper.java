package com.fiap.authbuyers.application.mapper;

import com.fiap.authbuyers.application.dto.response.BuyerResponse;
import com.fiap.authbuyers.domain.entity.Buyer;

public final class BuyerMapper {

    private BuyerMapper() {
    }

    public static BuyerResponse toResponse(Buyer buyer) {
        return new BuyerResponse(
                buyer.getId(),
                buyer.getName(),
                buyer.getEmail(),
                buyer.getCreatedAt(),
                buyer.getUpdatedAt()
        );
    }
}
