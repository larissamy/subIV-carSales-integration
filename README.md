# subIV-carSales-integration

Serviço de venda de veículos da Fase 4 do Tech Challenge SOAT.

Este repositório isola os endpoints de listagem, compra e webhook de pagamento em um serviço próprio, com banco de dados segregado. A comunicação com o software principal (`subIV-carSales`) é feita por requisições HTTP.

## Responsabilidade deste serviço

- Listar veículos à venda ordenados por preço, do mais barato para o mais caro.
- Listar veículos vendidos ordenados por preço, do mais barato para o mais caro.
- Efetuar a venda de um veículo usando CPF da pessoa compradora e data da venda.
- Gerar pagamento pendente com código de pagamento.
- Receber webhook da entidade processadora de pagamento.
- Confirmar ou cancelar a venda, atualizando o status do veículo no serviço principal via HTTP.
- Persistir vendas e pagamentos em banco próprio.

O serviço mantém os endpoints de autenticação/compradores herdados da Fase 3 como apoio, mas o fluxo da Fase 4 usa CPF no payload da compra.

## Banco de dados

Por padrão, o serviço usa SQLite em arquivo:

```yaml
jdbc:sqlite:sales-service.db
```

No Docker/Kubernetes, o arquivo fica em `/data/sales-service.db`.

## Comunicação HTTP

Variável de ambiente usada para apontar para o serviço principal:

```text
APP_CARS_SERVICE_URL=http://localhost:8080
```

No Kubernetes:

```text
APP_CARS_SERVICE_URL=http://carsales-api
```

## Endpoints da Fase 4

```http
GET /api/sales/cars/available
GET /api/sales/cars/sold
POST /api/sales/purchase
POST /api/webhooks/payments
GET /api/payments/{paymentCode}
```

### Comprar veículo

```json
{
  "carId": "uuid-do-veiculo",
  "buyerCpf": "12345678901",
  "saleDate": "2026-06-12T10:00:00Z"
}
```

A compra reserva o veículo no serviço principal e cria pagamento com status `PENDING`.

### Webhook de pagamento

```json
{
  "paymentCode": "123-ABC",
  "status": 1
}
```

Status aceitos:

```text
0 = PENDING
1 = PAID
2 = CANCELLED
```

Quando o pagamento é `PAID`, o serviço chama o `subIV-carSales` e marca o veículo como `SOLD`.

Quando o pagamento é `CANCELLED`, o serviço chama o `subIV-carSales` e libera o veículo para `AVAILABLE`.

## Rodando localmente

Suba primeiro o serviço principal:

```bash
cd ../subIV-carSales
mvn spring-boot:run
```

Depois suba este serviço:

```bash
cd ../subIV-carSales-integration
mvn spring-boot:run
```

Swagger:

```text
http://localhost:8081/swagger-ui/index.html
```

## Rodando com Docker

```bash
docker build -t subiv-carsales-integration:local .
docker run -p 8081:8081   -e APP_CARS_SERVICE_URL=http://host.docker.internal:8080   subiv-carsales-integration:local
```

Ou:

```bash
docker compose up --build
```

## Testes e cobertura

```bash
mvn clean verify
```

Relatório JaCoCo:

```text
target/site/jacoco/index.html
```

A regra de cobertura está configurada para mínimo de 80%.

## Kubernetes

```bash
kubectl apply -f k8s/
kubectl get pods
kubectl port-forward svc/vehicle-sales-service 8081:80
```

## Fluxo ponta-a-ponta sugerido para o vídeo

1. Subir `subIV-carSales` na porta 8080.
2. Subir `subIV-carSales-integration` na porta 8081.
3. Cadastrar veículo no `subIV-carSales`.
4. Listar veículos disponíveis pelo `subIV-carSales-integration`.
5. Comprar o veículo pelo `subIV-carSales-integration`.
6. Consultar o pagamento gerado.
7. Enviar webhook com status `PAID`.
8. Listar veículos vendidos pelo `subIV-carSales-integration`.
9. Mostrar os testes rodando com `mvn clean verify`.
10. Mostrar relatório de cobertura JaCoCo.

## CI/CD

O workflow em `.github/workflows/ci.yml` executa build, testes, cobertura, validação de Kubernetes e etapa de deploy após merge na branch principal.
