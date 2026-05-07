# 💸 PicPay Transfer API

API RESTful desenvolvida como solução para o [Desafio Backend PicPay](https://github.com/PicPay/picpay-desafio-backend), simulando um sistema simplificado de transferências financeiras entre usuários.

## 🚀 Tecnologias

- **Java 21**
- **Spring Boot 3.2**
- **Spring Data JPA**
- **H2 Database** (em memória)
- **Bean Validation**
- **JUnit 5 + Mockito**
- **Docker + Docker Compose**
- **Maven**

## 📋 Regras de Negócio

- Existem dois tipos de usuário: **Comum** e **Lojista**
- Lojistas **só recebem** transferências — nunca enviam
- Usuários comuns podem transferir entre si e para lojistas
- Antes de cada transferência, o saldo do pagador é validado
- A transferência consulta um **serviço autorizador externo** antes de ser concluída
- Em caso de qualquer inconsistência, a transação é **revertida** automaticamente
- Após a transferência, uma **notificação** é enviada ao recebedor (falha na notificação não reverte a transferência)

## 🗂️ Estrutura do Projeto

```
src/main/java/com/picpay/transfer/
├── client/
│   ├── AuthorizerClient.java       # Integração com autorizador externo
│   └── NotificationClient.java     # Integração com serviço de notificação
├── config/
│   └── RestTemplateConfig.java     # Configuração do RestTemplate
├── controller/
│   └── TransferController.java     # Endpoint POST /transfer
├── domain/
│   ├── entity/
│   │   ├── User.java
│   │   ├── Wallet.java
│   │   └── Transfer.java
│   └── enums/
│       └── UserType.java
├── dto/
│   ├── request/TransferRequest.java
│   └── response/TransferResponse.java
├── exception/
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
├── repository/
│   ├── UserRepository.java
│   ├── WalletRepository.java
│   └── TransferRepository.java
└── service/
    └── TransferService.java        # Regras de negócio + @Transactional
```

## ▶️ Como Executar

### Pré-requisitos
- Java 21+
- Maven 3.8+
- Docker (opcional)

### Rodando localmente

```bash
# Clone o repositório
git clone https://github.com/seu-usuario/picpay-transfer-api.git
cd picpay-transfer-api

# Execute com Maven
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`

### Rodando com Docker

```bash
docker-compose up --build
```

## 🔌 Endpoint

### `POST /transfer`

**Request:**
```json
{
  "value": 100.00,
  "payer": 1,
  "payee": 2
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "value": 100.00,
  "payerId": 1,
  "payeeId": 2,
  "createdAt": "2024-05-01T10:30:00"
}
```

**Possíveis erros (4xx):**

| Status | Situação |
|--------|----------|
| 400 | Corpo da requisição inválido |
| 404 | Usuário ou carteira não encontrados |
| 422 | Saldo insuficiente, lojista enviando, transferência não autorizada |
| 503 | Serviço autorizador indisponível |

## 🧪 Dados de Teste (populados automaticamente)

| ID | Nome | Tipo | Saldo |
|----|------|------|-------|
| 1 | Alice Souza | Comum | R$ 1.000,00 |
| 2 | Bruno Lima | Comum | R$ 500,00 |
| 3 | Loja do Zé | Lojista | R$ 0,00 |

## 🗃️ H2 Console

Acesse o banco em memória pelo navegador:

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:picpaydb`
- User: `sa`
- Password: _(em branco)_

## 🧪 Rodando os Testes

```bash
./mvnw test
```

## 📝 Decisões Técnicas

- **`@Transactional` no Service**: garante que débito + crédito + persistência ocorram atomicamente — qualquer erro reverte tudo
- **Notificação best-effort**: a falha no envio de notificação é apenas logada, sem reverter a transferência, conforme a especificação do desafio
- **`record` para DTOs**: mais conciso e imutável por padrão
- **`GlobalExceptionHandler`**: respostas de erro padronizadas em toda a API
