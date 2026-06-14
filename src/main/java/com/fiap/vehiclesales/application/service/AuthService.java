package com.fiap.vehiclesales.application.service;

import com.fiap.vehiclesales.application.dto.request.LoginRequest;
import com.fiap.vehiclesales.application.dto.request.RegisterBuyerRequest;
import com.fiap.vehiclesales.application.dto.response.AuthResponse;
import com.fiap.vehiclesales.application.dto.response.BuyerResponse;
import com.fiap.vehiclesales.application.mapper.BuyerMapper;
import com.fiap.vehiclesales.domain.entity.Buyer;
import com.fiap.vehiclesales.domain.exception.BusinessException;
import com.fiap.vehiclesales.domain.exception.NotFoundException;
import com.fiap.vehiclesales.domain.repository.BuyerRepository;
import com.fiap.vehiclesales.infrastructure.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final BuyerRepository buyerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(BuyerRepository buyerRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.buyerRepository = buyerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public BuyerResponse register(RegisterBuyerRequest request) {
        buyerRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .ifPresent(existing -> {
                    throw new BusinessException("buyer with this email already exists");
                });

        buyerRepository.findByCpf(request.getCpf().replaceAll("\\D", ""))
                .ifPresent(existing -> {
                    throw new BusinessException("buyer with this cpf already exists");
                });

        Buyer buyer = new Buyer();
        buyer.setName(request.getName().trim());
        buyer.setEmail(request.getEmail().trim().toLowerCase());
        buyer.setCpf(request.getCpf().replaceAll("\\D", ""));
        buyer.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        return BuyerMapper.toResponse(buyerRepository.save(buyer));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Buyer buyer = buyerRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BusinessException("invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), buyer.getPasswordHash())) {
            throw new BusinessException("invalid credentials");
        }

        String token = jwtService.generateToken(buyer);
        long expiresInSeconds = jwtService.getExpirationSeconds();
        return new AuthResponse(token, "Bearer", expiresInSeconds, BuyerMapper.toResponse(buyer));
    }

    @Transactional(readOnly = true)
    public BuyerResponse me(String email) {
        Buyer buyer = buyerRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new NotFoundException("buyer not found"));
        return BuyerMapper.toResponse(buyer);
    }
}
