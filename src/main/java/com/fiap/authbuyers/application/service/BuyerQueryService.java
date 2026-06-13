package com.fiap.authbuyers.application.service;

import com.fiap.authbuyers.application.dto.response.BuyerResponse;
import com.fiap.authbuyers.application.mapper.BuyerMapper;
import com.fiap.authbuyers.domain.exception.NotFoundException;
import com.fiap.authbuyers.domain.repository.BuyerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BuyerQueryService {

    private final BuyerRepository buyerRepository;

    public BuyerQueryService(BuyerRepository buyerRepository) {
        this.buyerRepository = buyerRepository;
    }

    @Transactional(readOnly = true)
    public List<BuyerResponse> findAll() {
        return buyerRepository.findAll().stream()
                .map(BuyerMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BuyerResponse findById(UUID id) {
        return buyerRepository.findById(id)
                .map(BuyerMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("buyer not found"));
    }
}
