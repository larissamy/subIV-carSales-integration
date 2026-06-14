# subIV-carSales-integration

Serviço de venda de veículos da Fase 4 do Tech Challenge SOAT.

Este repositório isola os endpoints de listagem, compra e webhook de pagamento em um serviço próprio, com banco de dados segregado. A comunicação com o software principal (`subIV-carSales`) é feita por requisições HTTP.

## Responsabilidade deste serviço

- Listar veículos à venda ordenados por preço, do mais barato para o mais caro.
- Listar veículos vendidos ordenados por preço, do mais barato para o mais caro.
- Efetuar a venda de um veículo usando a pessoa compradora autenticada e a data da venda.
- Gerar pagamento pendente com código de pagamento.
- Receber webhook da entidade processadora de pagamento.
- Confirmar ou cancelar a venda, atualizando o status do veículo no serviço principal via HTTP.
- Persistir vendas e pagamentos em banco próprio.

O serviço mantém cadastro/login de compradores como apoio ao fluxo da Fase 4. O CPF fica no cadastro do comprador e a compra usa os dados do comprador autenticado.

## Banco de dados e persistência real

Por padrão, o serviço usa SQLite em arquivo:

```yaml
jdbc:sqlite:sales-service.db
```

No Docker e no Kubernetes, o arquivo fica em:

```text
/data/sales-service.db
```

A persistência foi configurada para não depender de banco em memória:

- execução local: arquivo `sales-service.db`;
- Docker Compose: volume nomeado `sales-service-data`;
- Kubernetes: `PersistentVolume` + `PersistentVolumeClaim` em `k8s/pvc.yaml`.

Isso mantém vendas e pagamentos mesmo após reiniciar container/pod.

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

### Cadastro e login do comprador

```json
{
  "name": "Larissa Test",
  "email": "larissa.test@example.com",
  "cpf": "12345678901",
  "password": "Senha123"
}
```

Depois do login, copie o token retornado. No Swagger, clique em **Authorize** e cole apenas o valor do token.

Em Postman/cURL, envie o header completo:

```text
Authorization: Bearer SEU_TOKEN_AQUI
```

### Comprar veículo

A compra exige autenticação via Bearer Token. O CPF usado na venda vem do comprador logado.

```json
{
  "carId": "uuid-do-veiculo",
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
docker run -p 8081:8081   -v sales-service-data:/data   -e APP_CARS_SERVICE_URL=http://host.docker.internal:8080   subiv-carsales-integration:local
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
kubectl get pvc
kubectl port-forward svc/vehicle-sales-service 8081:80
```

Os manifests incluem:

- `deployment.yaml`;
- `service.yaml`;
- `configmap.yaml`;
- `secret.yaml`;
- `pvc.yaml` com `PersistentVolume` e `PersistentVolumeClaim`.

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
11. Mostrar Actions com build, push Docker e deploy smoke test passando.

## CI/CD

O workflow `.github/workflows/ci.yml` executa:

1. build, testes e cobertura com `mvn clean verify`;
2. validação dos manifests Kubernetes com `kind`;
3. build da imagem Docker em Pull Requests;
4. build e push da imagem Docker em merges/pushes para `main` ou `develop`;
5. deploy smoke test em Kubernetes local temporário com `kind`;
6. deploy opcional para cluster real quando a variável `ENABLE_REAL_K8S_DEPLOY=true` estiver configurada.

### Secrets necessários para push no Docker Hub

Configure no GitHub em `Settings > Secrets and variables > Actions`:

```text
DOCKERHUB_USERNAME
DOCKERHUB_TOKEN
```

A imagem publicada usa o nome:

```text
larissay/subiv-carsales-integration:latest
larissay/subiv-carsales-integration:<github-sha>
```

### Deploy em cluster real

Para habilitar o deploy real, configure:

```text
Repository variable: ENABLE_REAL_K8S_DEPLOY=true
Repository secret: KUBE_CONFIG=<kubeconfig em base64>
```

Sem essa variável, o workflow ainda executa o deploy smoke test usando `kind`, que serve como evidência de deploy efetivo na esteira.
