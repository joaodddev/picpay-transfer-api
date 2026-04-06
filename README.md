# API de Transferências - PicPay

API REST de transferências financeiras entre usuários, desenvolvida com **Java 21 + Spring Boot 3 + H2 (banco em memória)**.

## Tecnologias

- Java 21
- Spring Boot 3.2
- Spring Data JPA
- H2 Database (em memória)
- Lombok
- Maven

---

## Como executar

### Pré-requisitos
- Java 21 instalado
- Maven instalado (ou use o wrapper `./mvnw`)

### Rodando o projeto

```bash
# Clonar/extrair o projeto e entrar na pasta
cd transfer-api

# Compilar e rodar
./mvnw spring-boot:run

# Ou com Maven instalado
mvn spring-boot:run
```

A API sobe em: **http://localhost:8080**

Console H2 (banco em memória): **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:transferdb`
- User: `sa`
- Password: *(vazio)*

---

## Dados iniciais (seed)

O banco é populado automaticamente com:

| ID | Nome               | CPF/CNPJ       | Tipo     | Saldo     |
|----|--------------------|----------------|----------|-----------|
| 1  | Alice Silva        | 12345678901    | COMMON   | R$ 500,00 |
| 2  | Bob Santos         | 98765432100    | COMMON   | R$ 300,00 |
| 3  | Loja do Carlos     | 12345678000195 | MERCHANT | R$ 1000,00|
| 4  | Mercado da Maria   | 98765432000110 | MERCHANT | R$ 2500,00|

---

## Endpoints

### Usuários

#### Listar todos os usuários
```
GET /users
```

#### Buscar usuário por ID
```
GET /users/{id}
```

#### Criar usuário
```
POST /users
Content-Type: application/json

{
  "fullName": "João Pereira",
  "cpfCnpj": "11122233344",
  "email": "joao@email.com",
  "password": "senha123",
  "balance": 250.00,
  "userType": "COMMON"
}
```
> `userType`: `COMMON` (usuário comum) ou `MERCHANT` (lojista)

---

### Transferências

#### Realizar transferência
```
POST /transfer
Content-Type: application/json

{
  "payer": 1,
  "payee": 2,
  "value": 50.00
}
```

**Regras de negócio:**
- Apenas usuários `COMMON` podem enviar transferências
- Lojistas (`MERCHANT`) só podem **receber**
- Saldo do pagador deve ser suficiente
- A transferência consulta um **autorizador externo** antes de efetivar
- Após a transferência, o recebedor é **notificado** de forma assíncrona

#### Listar todas as transferências
```
GET /transfer
```

#### Transferências por pagador
```
GET /transfer/payer/{payerId}
```

#### Transferências por recebedor
```
GET /transfer/payee/{payeeId}
```

---

## Exemplos com curl

```bash
# Listar usuários
curl http://localhost:8080/users

# Transferência de Alice (ID 1) para Bob (ID 2)
curl -X POST http://localhost:8080/transfer \
  -H "Content-Type: application/json" \
  -d '{"payer": 1, "payee": 2, "value": 100.00}'

# Tentativa inválida: lojista tentando enviar (deve retornar 403)
curl -X POST http://localhost:8080/transfer \
  -H "Content-Type: application/json" \
  -d '{"payer": 3, "payee": 1, "value": 50.00}'

# Tentativa com saldo insuficiente (deve retornar 422)
curl -X POST http://localhost:8080/transfer \
  -H "Content-Type: application/json" \
  -d '{"payer": 2, "payee": 1, "value": 99999.00}'
```

---

## Respostas de erro

| Status | Situação |
|--------|----------|
| 400    | Dados inválidos (validação) |
| 403    | Lojista tentando transferir / Autorizador negou |
| 404    | Usuário não encontrado |
| 422    | Saldo insuficiente |
| 500    | Erro interno |

---

## Serviços externos (simulados)

- **Autorizador**: `https://util.devi.tools/api/v2/authorize`
- **Notificador**: `https://util.devi.tools/api/v1/notify`

> Ambos são APIs públicas de mock. Se estiverem indisponíveis, a transferência será negada (autorizador) ou a falha será ignorada (notificador — não reverte a operação).

---

## Executar testes

```bash
mvn test
```
