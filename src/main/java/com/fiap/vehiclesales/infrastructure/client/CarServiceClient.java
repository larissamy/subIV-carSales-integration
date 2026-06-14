package com.fiap.vehiclesales.infrastructure.client;

import com.fiap.vehiclesales.application.dto.response.CarResponse;
import com.fiap.vehiclesales.domain.exception.BusinessException;
import com.fiap.vehiclesales.domain.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class CarServiceClient {

    private final RestClient restClient;

    public CarServiceClient(RestClient.Builder builder, CarSalesProperties properties) {
        this.restClient = builder.baseUrl(properties.serviceUrl()).build();
    }

    public CarResponse getCarById(UUID carId) {
        try {
            return restClient.get()
                    .uri("/api/integration/cars/{id}", carId)
                    .retrieve()
                    .body(CarResponse.class);
        } catch (RestClientResponseException ex) {
            throw mapException(ex, "Erro ao consultar veículo no serviço principal.");
        }
    }

    public List<CarResponse> getAvailableCars() {
        return listCars("/api/integration/cars/available");
    }

    public List<CarResponse> getSoldCars() {
        return listCars("/api/integration/cars/sold");
    }

    public CarResponse reserveCar(UUID carId) {
        return patchCar("/api/integration/cars/{id}/reserve", carId);
    }

    public CarResponse markCarAsSold(UUID carId) {
        return patchCar("/api/integration/cars/{id}/sold", carId);
    }

    public CarResponse makeCarAvailable(UUID carId) {
        return patchCar("/api/integration/cars/{id}/available", carId);
    }

    private List<CarResponse> listCars(String uri) {
        try {
            var response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(CarResponse[].class);
            return response == null ? List.of() : Arrays.asList(response);
        } catch (RestClientResponseException ex) {
            throw mapException(ex, "Erro ao listar veículos no serviço principal.");
        }
    }

    private CarResponse patchCar(String uri, UUID carId) {
        try {
            return restClient.patch()
                    .uri(uri, carId)
                    .retrieve()
                    .body(CarResponse.class);
        } catch (RestClientResponseException ex) {
            throw mapException(ex, "Erro ao atualizar veículo no serviço principal.");
        }
    }

    private RuntimeException mapException(RestClientResponseException ex, String fallbackMessage) {
        if (ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
            return new NotFoundException("Veículo não encontrado no serviço principal.");
        }
        return new BusinessException(fallbackMessage + " Status: " + ex.getStatusCode().value());
    }
}
