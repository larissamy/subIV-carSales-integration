package com.fiap.authbuyers.application.service;

import com.fiap.authbuyers.application.dto.request.PaymentWebhookRequest;
import com.fiap.authbuyers.application.dto.response.PaymentResponse;
import com.fiap.authbuyers.domain.entity.Payment;
import com.fiap.authbuyers.domain.enums.PaymentStatus;
import com.fiap.authbuyers.domain.exception.NotFoundException;
import com.fiap.authbuyers.domain.repository.PaymentRepository;
import com.fiap.authbuyers.domain.repository.SaleRepository;
import com.fiap.authbuyers.infrastructure.client.CarServiceClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SaleRepository saleRepository;
    private final CarServiceClient carServiceClient;

    public PaymentService(PaymentRepository paymentRepository,
                          SaleRepository saleRepository,
                          CarServiceClient carServiceClient) {
        this.paymentRepository = paymentRepository;
        this.saleRepository = saleRepository;
        this.carServiceClient = carServiceClient;
    }

    /**
     * Webhook endpoint:
     * status: 0 = PENDING, 1 = PAID, 2 = CANCELLED
     */
    @Transactional
    public void confirm(PaymentWebhookRequest input) {
        if (input.status() == null || input.status() < 0 || input.status() > 2) {
            throw new IllegalArgumentException("Invalid payment status (expected 0, 1 or 2)");
        }

        var payment = paymentRepository.getByPaymentCode(input.paymentCode())
                .orElseThrow(() -> new NotFoundException("Pagamento com código " + input.paymentCode() + " não encontrado"));

        PaymentStatus incoming = switch (input.status()) {
            case 0 -> PaymentStatus.PENDING;
            case 1 -> PaymentStatus.PAID;
            case 2 -> PaymentStatus.CANCELLED;
            default -> throw new IllegalStateException("Unexpected value: " + input.status());
        };

        if (incoming == PaymentStatus.PAID) {
            payment.confirmPayment();
        } else if (incoming == PaymentStatus.CANCELLED) {
            payment.cancelPayment();
        }

        paymentRepository.update(payment);

        var sale = saleRepository.getByPaymentId(payment.getId())
                .orElseThrow(() -> new NotFoundException("Venda para o pagamento " + payment.getPaymentCode() + " não encontrada"));

        if (incoming == PaymentStatus.PAID) {
            carServiceClient.markCarAsSold(sale.getCarId());
        } else if (incoming == PaymentStatus.CANCELLED) {
            carServiceClient.makeCarAvailable(sale.getCarId());
        }
    }

    public Optional<PaymentResponse> getPaymentByPaymentCode(String paymentCode) {
        return paymentRepository.getByPaymentCode(paymentCode)
                .map(this::toResponse);
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getAmount(),
                payment.getPaymentCode(),
                payment.getPaymentStatus().name()
        );
    }
}
