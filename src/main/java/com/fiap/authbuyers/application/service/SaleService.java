package com.fiap.authbuyers.application.service;

import com.fiap.authbuyers.application.dto.request.PurchaseRequest;
import com.fiap.authbuyers.application.dto.response.CarResponse;
import com.fiap.authbuyers.application.dto.response.SaleResponse;
import com.fiap.authbuyers.domain.entity.Payment;
import com.fiap.authbuyers.domain.entity.Sale;
import com.fiap.authbuyers.domain.exception.BusinessException;
import com.fiap.authbuyers.domain.repository.PaymentRepository;
import com.fiap.authbuyers.domain.repository.SaleRepository;
import com.fiap.authbuyers.infrastructure.client.CarServiceClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SaleService {

    private final CarServiceClient carServiceClient;
    private final SaleRepository saleRepository;
    private final PaymentRepository paymentRepository;

    public SaleService(CarServiceClient carServiceClient,
                       SaleRepository saleRepository,
                       PaymentRepository paymentRepository) {
        this.carServiceClient = carServiceClient;
        this.saleRepository = saleRepository;
        this.paymentRepository = paymentRepository;
    }

    public List<CarResponse> getAvailableCars() {
        return carServiceClient.getAvailableCars();
    }

    public List<CarResponse> getSoldCars() {
        return carServiceClient.getSoldCars();
    }

    @Transactional
    public SaleResponse purchase(PurchaseRequest request) {
        var car = carServiceClient.getCarById(request.carId());
        if (!"AVAILABLE".equals(car.status())) {
            throw new BusinessException("O carro " + car.licensePlate() + " não está disponível para venda. Status atual: " + car.status());
        }

        carServiceClient.reserveCar(car.id());

        var payment = Payment.create(car.price());
        var sale = Sale.create(
                car.id(),
                request.buyerCpf(),
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
