package com.fiap.vehiclesales.application.service;

import com.fiap.vehiclesales.application.dto.request.PurchaseRequest;
import com.fiap.vehiclesales.application.dto.response.CarResponse;
import com.fiap.vehiclesales.application.dto.response.SaleResponse;
import com.fiap.vehiclesales.domain.entity.Payment;
import com.fiap.vehiclesales.domain.entity.Sale;
import com.fiap.vehiclesales.domain.exception.BusinessException;
import com.fiap.vehiclesales.domain.exception.NotFoundException;
import com.fiap.vehiclesales.domain.repository.BuyerRepository;
import com.fiap.vehiclesales.domain.repository.PaymentRepository;
import com.fiap.vehiclesales.domain.repository.SaleRepository;
import com.fiap.vehiclesales.infrastructure.client.CarServiceClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SaleService {

    private final CarServiceClient carServiceClient;
    private final SaleRepository saleRepository;
    private final PaymentRepository paymentRepository;
    private final BuyerRepository buyerRepository;

    public SaleService(CarServiceClient carServiceClient,
                       SaleRepository saleRepository,
                       PaymentRepository paymentRepository,
                       BuyerRepository buyerRepository) {
        this.carServiceClient = carServiceClient;
        this.saleRepository = saleRepository;
        this.paymentRepository = paymentRepository;
        this.buyerRepository = buyerRepository;
    }

    public List<CarResponse> getAvailableCars() {
        return carServiceClient.getAvailableCars();
    }

    public List<CarResponse> getSoldCars() {
        return carServiceClient.getSoldCars();
    }

    @Transactional
    public SaleResponse purchase(PurchaseRequest request, String buyerEmail) {
        var buyer = buyerRepository.findByEmail(buyerEmail.trim().toLowerCase())
                .orElseThrow(() -> new NotFoundException("buyer not found"));

        var car = carServiceClient.getCarById(request.carId());
        if (!"AVAILABLE".equals(car.status())) {
            throw new BusinessException("O carro " + car.licensePlate() + " não está disponível para venda. Status atual: " + car.status());
        }

        carServiceClient.reserveCar(car.id());

        var payment = Payment.create(car.price());
        var sale = Sale.create(
                car.id(),
                buyer.getCpf(),
                car.price(),
                request.saleDate()
        );
        sale.assignPaymentId(payment.getId());

        paymentRepository.add(payment);
        saleRepository.add(sale);

        return toResponse(sale, payment);
    }

    private SaleResponse toResponse(Sale sale, Payment payment) {
        return new SaleResponse(
                sale.getId(),
                sale.getCarId(),
                sale.getBuyerCpf(),
                sale.getPrice(),
                payment.getPaymentCode(),
                sale.getCreatedAt(),
                payment.getPaymentStatus().name()
        );
    }
}
