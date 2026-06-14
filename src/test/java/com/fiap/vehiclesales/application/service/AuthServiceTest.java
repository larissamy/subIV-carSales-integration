package com.fiap.vehiclesales.application.service;

import com.fiap.vehiclesales.application.dto.request.LoginRequest;
import com.fiap.vehiclesales.application.dto.request.RegisterBuyerRequest;
import com.fiap.vehiclesales.domain.entity.Buyer;
import com.fiap.vehiclesales.domain.exception.BusinessException;
import com.fiap.vehiclesales.domain.exception.NotFoundException;
import com.fiap.vehiclesales.domain.repository.BuyerRepository;
import com.fiap.vehiclesales.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private BuyerRepository buyerRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService service;

    @BeforeEach
    void setup() {
        buyerRepository = mock(BuyerRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        service = new AuthService(buyerRepository, passwordEncoder, jwtService);
    }

    @Test
    void shouldRegisterBuyerNormalizingEmailAndCpf() {
        RegisterBuyerRequest request = registerRequest(" Larissa Test ", " LARISSA.TEST@EXAMPLE.COM ", "123.456.789-01", "Senha123");

        when(buyerRepository.findByEmail("larissa.test@example.com")).thenReturn(Optional.empty());
        when(buyerRepository.findByCpf("12345678901")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Senha123")).thenReturn("encoded-password");
        when(buyerRepository.save(any(Buyer.class))).thenAnswer(invocation -> {
            Buyer buyer = invocation.getArgument(0);
            buyer.prePersist();
            return buyer;
        });

        var response = service.register(request);

        assertEquals("Larissa Test", response.name());
        assertEquals("larissa.test@example.com", response.email());
        assertEquals("12345678901", response.cpf());
        verify(buyerRepository).findByEmail("larissa.test@example.com");
        verify(buyerRepository).findByCpf("12345678901");
        verify(passwordEncoder).encode("Senha123");
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        RegisterBuyerRequest request = registerRequest("Larissa", "larissa@example.com", "12345678901", "Senha123");
        when(buyerRepository.findByEmail("larissa@example.com")).thenReturn(Optional.of(buyer("Larissa", "larissa@example.com", "12345678901", "hash")));

        assertThrows(BusinessException.class, () -> service.register(request));
    }

    @Test
    void shouldThrowWhenCpfAlreadyExists() {
        RegisterBuyerRequest request = registerRequest("Larissa", "larissa@example.com", "12345678901", "Senha123");
        when(buyerRepository.findByEmail("larissa@example.com")).thenReturn(Optional.empty());
        when(buyerRepository.findByCpf("12345678901")).thenReturn(Optional.of(buyer("Outra", "outra@example.com", "12345678901", "hash")));

        assertThrows(BusinessException.class, () -> service.register(request));
    }

    @Test
    void shouldLoginAndReturnBearerToken() {
        Buyer buyer = buyer("Larissa", "larissa@example.com", "12345678901", "encoded-password");
        LoginRequest request = loginRequest(" LARISSA@EXAMPLE.COM ", "Senha123");

        when(buyerRepository.findByEmail("larissa@example.com")).thenReturn(Optional.of(buyer));
        when(passwordEncoder.matches("Senha123", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(buyer)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(7200L);

        var response = service.login(request);

        assertEquals("jwt-token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals(7200L, response.expiresInSeconds());
        assertEquals("larissa@example.com", response.buyer().email());
    }

    @Test
    void shouldThrowWhenLoginEmailDoesNotExist() {
        LoginRequest request = loginRequest("missing@example.com", "Senha123");
        when(buyerRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.login(request));
    }

    @Test
    void shouldThrowWhenPasswordDoesNotMatch() {
        Buyer buyer = buyer("Larissa", "larissa@example.com", "12345678901", "encoded-password");
        LoginRequest request = loginRequest("larissa@example.com", "wrong-password");

        when(buyerRepository.findByEmail("larissa@example.com")).thenReturn(Optional.of(buyer));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThrows(BusinessException.class, () -> service.login(request));
    }

    @Test
    void shouldReturnAuthenticatedBuyer() {
        Buyer buyer = buyer("Larissa", "larissa@example.com", "12345678901", "hash");
        when(buyerRepository.findByEmail("larissa@example.com")).thenReturn(Optional.of(buyer));

        var response = service.me(" LARISSA@EXAMPLE.COM ");

        assertEquals("larissa@example.com", response.email());
        assertEquals("12345678901", response.cpf());
    }

    @Test
    void shouldThrowWhenAuthenticatedBuyerDoesNotExist() {
        when(buyerRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.me("missing@example.com"));
    }

    private RegisterBuyerRequest registerRequest(String name, String email, String cpf, String password) {
        RegisterBuyerRequest request = new RegisterBuyerRequest();
        request.setName(name);
        request.setEmail(email);
        request.setCpf(cpf);
        request.setPassword(password);
        return request;
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private Buyer buyer(String name, String email, String cpf, String passwordHash) {
        Buyer buyer = new Buyer();
        buyer.setName(name);
        buyer.setEmail(email);
        buyer.setCpf(cpf);
        buyer.setPasswordHash(passwordHash);
        buyer.prePersist();
        return buyer;
    }
}
