# FIAP - Sub IV - carSales Integration

Serviço isolado de venda de veículos da plataforma de revenda de veículos.

Este repositório isola os endpoints de listagem, compra e webhook de pagamento em um serviço próprio, com banco de dados segregado. A comunicação com o software principal [`subIV-carSales`](https://github.com/larissamy/subIV-carSales) é feita por requisições HTTP, respeitando a separação de responsabilidades da arquitetura de microsserviços.

---

## Responsabilidade deste serviço

O `subIV-carSales-integration` é o **serviço isolado de venda de veículos**. Ele é responsável por:

- listar veículos disponíveis para venda;
- listar veículos vendidos;
- efetuar a compra de um veículo;
- receber webhook de pagamento;
- consultar pagamentos;
- autenticar compradores;
- gerar pagamento pendente com código de pagamento;
- confirmar ou cancelar a venda;
- atualizar o status do veículo no serviço principal via HTTP;
- persistir vendas, pagamentos e compradores em banco próprio.

O serviço mantém cadastro/login de compradores como apoio ao fluxo da Fase 4. O CPF fica no cadastro do comprador e a compra usa os dados do comprador autenticado.

---

## Arquitetura da solução

A solução é composta por dois serviços:

| Serviço | Responsabilidade | Porta |
|---|---|---|
| `subIV-carSales` | Serviço principal de veículos | `8080` |
| `subIV-carSales-integration` | Serviço isolado de vendas, listagens, compradores e webhook de pagamento | `8081` |

A comunicação entre os serviços acontece via **requisições HTTP**.

Este serviço consulta os veículos disponíveis, solicita reserva, confirma venda ou libera veículos chamando os endpoints de integração do `subIV-carSales`.

---

## Como executar o projeto

O `subIV-carSales-integration` é o serviço isolado de venda de veículos. Ele é responsável por:

- listar veículos disponíveis para venda;
- listar veículos vendidos;
- efetuar a compra de um veículo;
- receber webhook de pagamento;
- consultar pagamentos;
- autenticar compradores.

A API roda na porta `8081`.

Swagger disponível em:

```text
http://localhost:8081/swagger-ui/index.html
```

Este serviço se comunica via HTTP com o serviço principal `subIV-carSales`, que deve estar rodando na porta `8080`.

---

## Pré-requisito

Antes de subir este serviço, suba o `subIV-carSales`:

```bash
cd subIV-carSales
mvn spring-boot:run
```

O serviço principal deve estar disponível em:

```text
http://localhost:8080
```

---

## Banco de dados e persistência real

Este serviço utiliza um banco SQLite próprio:

```text
sales-service.db
```

Por padrão, em execução local, a aplicação usa:

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

Este banco **não é compartilhado** com o serviço principal de veículos. O serviço `subIV-carSales` utiliza outro banco, chamado `cars-main.db`.

A comunicação entre os serviços acontece exclusivamente via HTTP.

---

## Comunicação HTTP com o serviço principal

Variável de ambiente usada para apontar para o serviço principal:

```text
APP_CARS_SERVICE_URL=http://localhost:8080
```

No Kubernetes, a comunicação usa o nome do Service do `subIV-carSales`:

```text
APP_CARS_SERVICE_URL=http://carsales-api
```

---

## Endpoints principais

### Endpoints da Fase 4

```http
GET /api/sales/cars/available
GET /api/sales/cars/sold
POST /api/sales/purchase
POST /api/webhooks/payments
GET /api/payments/{paymentCode}
```

### Autenticação e compradores

```http
POST /auth/register
POST /auth/login
GET /auth/me
```

---

## Fluxo de autenticação

Para usar endpoints protegidos:

1. Registre um comprador em `/auth/register`;
2. Faça login em `/auth/login`;
3. Copie o token JWT retornado;
4. No Swagger, clique em `Authorize`;
5. Cole o token;
6. Execute os endpoints protegidos, como `/auth/me` e `/api/sales/purchase`.

No Swagger, cole apenas o valor do token no modal de autorização.

Em Postman/cURL, envie o header completo:

```text
Authorization: Bearer SEU_TOKEN_AQUI
```

### Cadastro de comprador

```json
{
  "name": "Larissa Test",
  "email": "larissa.test@example.com",
  "cpf": "12345678901",
  "password": "Senha123"
}
```

### Login

```json
{
  "email": "larissa.test@example.com",
  "password": "Senha123"
}
```

---

## Comprar veículo

A compra exige autenticação via Bearer Token. O CPF usado na venda vem do comprador logado.

```json
{
  "carId": "uuid-do-veiculo",
  "saleDate": "2026-06-12T10:00:00Z"
}
```

A compra reserva o veículo no serviço principal e cria pagamento com status `PENDING`.

---

## Webhook de pagamento

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

---

## Run local com Maven

### PowerShell

```powershell
$env:APP_CARS_SERVICE_URL="http://localhost:8080"
mvn clean package -DskipTests
mvn spring-boot:run
```

### Git Bash / Linux / macOS

```bash
export APP_CARS_SERVICE_URL=http://localhost:8080
mvn clean package -DskipTests
mvn spring-boot:run
```

Também é possível executar em uma única linha no Git Bash:

```bash
APP_CARS_SERVICE_URL=http://localhost:8080 mvn spring-boot:run
```

A aplicação ficará disponível em:

```text
http://localhost:8081/swagger-ui/index.html
```

---

## Run Docker - Opção A: Docker Compose

```bash
docker compose up --build
```

A API ficará disponível em:

```text
http://localhost:8081/swagger-ui/index.html
```

---

## Run Docker - Opção B: Docker manual

Como o serviço de integração precisa chamar o serviço principal, use `host.docker.internal` para acessar o `subIV-carSales` rodando localmente na porta `8080`.

```bash
mvn clean package -DskipTests
docker build -t subiv-carsales-integration:local .
docker run --rm -p 8081:8081 -v sales-service-data:/data -e APP_CARS_SERVICE_URL=http://host.docker.internal:8080 -e SPRING_DATASOURCE_URL=jdbc:sqlite:/data/sales-service.db subiv-carsales-integration:local
```

A API ficará disponível em:

```text
http://localhost:8081/swagger-ui/index.html
```

Caso esteja usando Linux e `host.docker.internal` não funcione, rode com:

```bash
docker run --rm -p 8081:8081 --add-host=host.docker.internal:host-gateway -v sales-service-data:/data -e APP_CARS_SERVICE_URL=http://host.docker.internal:8080 -e SPRING_DATASOURCE_URL=jdbc:sqlite:/data/sales-service.db subiv-carsales-integration:local
```

---

## Run Kubernetes

Antes de subir o serviço de integração no Kubernetes, garanta que o serviço principal `carsales-api` também esteja aplicado no cluster.

```bash
mvn clean package -DskipTests
docker build -t subiv-carsales-integration:local .
kubectl apply -f k8s/
kubectl set image deployment/vehicle-sales-service vehicle-sales-service=subiv-carsales-integration:local
kubectl get pods
kubectl port-forward svc/vehicle-sales-service 8081:80
```

A API ficará disponível em:

```text
http://localhost:8081/swagger-ui/index.html
```

> Observação: o comando `kubectl port-forward` mantém o terminal ocupado enquanto o túnel estiver ativo. Para encerrar, pressione `Ctrl + C`.

---

## Kubernetes

Os manifests Kubernetes ficam no diretório:

```text
k8s/
```

Arquivos principais:

- `deployment.yaml`;
- `service.yaml`;
- `configmap.yaml`;
- `secret.yaml`;
- `pvc.yaml`.

O deployment utiliza volume persistente para manter o banco SQLite em arquivo no caminho:

```text
/data/sales-service.db
```

O container roda como usuário não-root e o volume recebe ajuste de permissão por `initContainer`, permitindo que a aplicação grave o arquivo SQLite com segurança.

Comandos úteis:

```bash
kubectl apply -f k8s/
kubectl get pods
kubectl get svc
kubectl get pvc
kubectl port-forward svc/vehicle-sales-service 8081:80
```

---

## Testes e cobertura

Para rodar os testes automatizados e gerar o relatório de cobertura:

```bash
mvn clean verify
```

Relatório JaCoCo:

```text
target/site/jacoco/index.html
```

A cobertura é validada automaticamente pelo JaCoCo durante o build. O requisito mínimo da entrega é cobertura de pelo menos 80%.

---

## Docker Hub

A imagem Docker publicada pela esteira CI/CD usa o nome:

```text
larissay/subiv-carsales-integration
```

Tags publicadas:

```text
larissay/subiv-carsales-integration:latest
larissay/subiv-carsales-integration:<github-sha>
```

---

## CI/CD

O workflow principal fica em:

```text
.github/workflows/ci.yml
```

A esteira executa:

1. build, testes e cobertura com `mvn clean verify`;
2. upload do relatório JaCoCo como artefato;
3. validação dos manifests Kubernetes;
4. build da imagem Docker;
5. push da imagem Docker para o Docker Hub em pushes/merges para `main` ou `develop`;
6. deploy smoke test opcional em Kubernetes local temporário com `kind`;
7. deploy opcional para cluster Kubernetes real.

### Secrets necessários para push no Docker Hub

Configure no GitHub em:

```text
Settings > Secrets and variables > Actions
```

Secrets:

```text
DOCKERHUB_USERNAME
DOCKERHUB_TOKEN
```

### Deploy smoke test com kind

O deploy smoke test com `kind` é opcional e pode ser habilitado com a variável:

```text
ENABLE_KIND_DEPLOY=true
```

Quando desabilitado, a esteira continua validando build, testes, cobertura, manifests Kubernetes e publicação da imagem Docker.

### Deploy em cluster real

Para habilitar o deploy em um cluster Kubernetes real, configure:

```text
Repository variable: ENABLE_REAL_K8S_DEPLOY=true
Repository secret: KUBE_CONFIG=<kubeconfig em base64>
```

Sem essa configuração, o deploy real não é executado.

---

## Fluxo ponta-a-ponta sugerido para o vídeo

1. Subir `subIV-carSales` na porta 8080.
2. Subir `subIV-carSales-integration` na porta 8081.
3. Cadastrar veículo no `subIV-carSales`.
4. Listar veículos disponíveis pelo `subIV-carSales-integration`.
5. Registrar comprador e fazer login.
6. Autorizar o token JWT no Swagger.
7. Comprar o veículo pelo `subIV-carSales-integration`.
8. Consultar o pagamento gerado.
9. Enviar webhook com status `PAID`.
10. Listar veículos vendidos pelo `subIV-carSales-integration`.
11. Consultar o veículo no `subIV-carSales` e confirmar status `SOLD`.
12. Mostrar os testes rodando com `mvn clean verify`.
13. Mostrar relatório de cobertura JaCoCo.
14. Mostrar Actions com build, validação Kubernetes e push Docker.
15. Mostrar imagens publicadas no Docker Hub.

---

## Swagger

Com a aplicação rodando localmente, acesse:

```text
http://localhost:8081/swagger-ui/index.html
```
