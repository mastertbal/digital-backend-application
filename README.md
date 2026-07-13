# Digital Banking Backend Application

> A production-grade RESTful banking microservice built with Spring Boot 4 and Java 21, developed as the final project for the NAIJUG (Nigerian Java User Group) Bootcamp.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [API Reference](#api-reference)
- [Data Model](#data-model)
- [Testing](#testing)
- [Contributing](#contributing)

---

## Overview

The Digital Banking Backend Application is a RESTful API that powers core digital banking operations for a fictional Nigerian fintech platform. It handles customer onboarding, account management, fund transfers, card-based deposits, and payment reconciliation — all built on a clean layered architecture with double-entry bookkeeping at its core.

---

## Features

- **Customer Onboarding** — Register personal bank customers with full KYC-ready fields (NIN, BVN, address). Includes age verification (18+), duplicate email/phone detection, and Nigerian phone number format validation.
- **Account Management** — Automatically generates unique account numbers and provisions Tier 1 accounts upon registration.
- **Fund Transfers** — Transfers money between two existing accounts with balance validation and immediate double-entry ledger settlement.
- **Card Deposits** — Accepts card-based deposits with full card detail verification (card number, name, expiry, CVC). Transactions can resolve as `SUCCESSFUL` (201) or `PENDING` (202) based on card type.
- **Transaction Requery** — Simulates payment gateway reconciliation by re-querying a `PENDING` transaction. The outcome is randomly resolved to `SUCCESSFUL` or `DECLINED`, with corresponding ledger updates.
- **Double-Entry Bookkeeping** — Every financial transaction produces corresponding `DEBIT` and `CREDIT` ledger entries, ensuring full auditability.
- **Global Exception Handling** — Standardized error responses for `BadRequestException` (400) and `ResourceNotFoundException` (404) via `@ControllerAdvice`.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.0 |
| API | Spring Web MVC (REST) |
| ORM | Spring Data JPA / Hibernate |
| Database | PostgreSQL 16 |
| Connection Pooling | HikariCP (max 10 connections) |
| Validation | Jakarta Bean Validation |
| Code Generation | Lombok |
| Build Tool | Apache Maven |
| Containerization | Docker & Docker Compose |
| DB Admin UI | Adminer |
| Testing | JUnit 5 · Mockito 5 · Spring MockMvc |

---

## Architecture

The application follows a strict layered architecture:

```
HTTP Request
     │
     ▼
┌─────────────┐
│  Controller  │  Receives and validates HTTP payloads (@Valid)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Service   │  Orchestrates business logic (@Transactional)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Repository  │  Abstracts database access (Spring Data JPA)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ PostgreSQL  │  Persistent storage
└─────────────┘
```

**Key architectural decisions:**

- **JOINED inheritance** — `Customer` extends `User` using JPA's `JOINED` strategy, keeping user and customer data in separate tables while sharing a primary key.
- **Double-entry bookkeeping** — Every `Transaction` cascades `LedgerEntry` records (DEBIT + CREDIT) for full auditability and reconciliation support.
- **In-memory card mock** — `CardDetailsRepository` holds a small set of test cards in memory, simulating a card-scheme response without external dependencies.
- **Requery simulation** — `TransactionRequeryUtil` randomly picks `SUCCESSFUL` or `DECLINED`, mimicking real-world asynchronous payment reconciliation.
- **Generic response wrapper** — All endpoints return `ResponseWrapper<T>` ensuring a consistent `{ data, message, statusCode }` contract.

---

## Project Structure

```
src/
└── main/
│   └── java/com/groupa/digitalbackendapplication/
│       ├── DigitalBackendApplication.java
│       ├── controller/
│       │   ├── AccountController.java
│       │   └── TransactionController.java
│       ├── service/
│       │   ├── CustomerService.java
│       │   ├── TransactionService.java
│       │   ├── DepositService.java
│       │   └── impl/
│       │       ├── CustomerServiceImpl.java
│       │       ├── TransactionServiceImpl.java
│       │       └── CardDepositServiceImpl.java
│       ├── repository/
│       │   ├── CustomerRepository.java
│       │   ├── AccountRepository.java
│       │   ├── TransactionRepository.java
│       │   ├── CardDetailsRepository.java
│       │   └── LedgerEntryRepository.java
│       ├── domain/
│       │   ├── entities/
│       │   │   ├── User.java
│       │   │   ├── Customer.java
│       │   │   ├── Business.java
│       │   │   ├── Account.java
│       │   │   ├── Transaction.java
│       │   │   ├── LedgerEntry.java
│       │   │   ├── CardDetails.java
│       │   │   ├── AuditLog.java
│       │   │   └── KycEntity.java
│       │   ├── dto/
│       │   │   ├── request/
│       │   │   └── response/
│       │   └── enums/
│       ├── exceptions/
│       │   ├── GlobalExceptionHandler.java
│       │   ├── BadRequestException.java
│       │   ├── ResourceNotFoundException.java
│       │   └── ErrorResponse.java
│       └── utils/
│           ├── AccountUtil.java
│           ├── TransactionUtil.java
│           └── TransactionRequeryUtil.java
└── test/
    └── java/com/groupa/digitalbackendapplication/
        ├── controller/
        │   ├── AccountControllerTest.java
        │   └── TransactionControllerTest.java
        ├── service/
        │   ├── CustomerServiceImplTest.java
        │   ├── TransactionServiceImplTest.java
        │   └── CardDepositServiceImplTest.java
        └── utils/
            ├── AccountUtilTest.java
            └── TransactionRequeryUtilTest.java
```

---

## Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **Docker** and **Docker Compose**

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/apondi-art/digital-backend-application.git
cd digital-backend-application
```

### 2. Set environment variables

Create a `.env` file in the project root or export the variables in your shell:

```bash
export db=digitalbank
export usernames=postgres
export password=yourpassword
export POSTGRES_DB=digitalbank
export POSTGRES_USER=postgres
export POSTGRES_PASSWORD=yourpassword
```

### 3. Start the database

```bash
docker-compose up -d
```

- PostgreSQL will be available on `localhost:5433`
- Adminer (database UI) will be available at `http://localhost:8082`

### 4. Build the application

```bash
mvn clean package -DskipTests
```

### 5. Run the application

```bash
java -jar target/digital-backend-application-0.0.1-SNAPSHOT.jar
```

The application starts on `http://localhost:8080`.

---

## Environment Variables

| Variable | Description | Example |
|---|---|---|
| `db` | PostgreSQL database name | `digitalbank` |
| `usernames` | PostgreSQL username | `postgres` |
| `password` | PostgreSQL password | `secret` |
| `POSTGRES_DB` | Docker Postgres DB name | `digitalbank` |
| `POSTGRES_USER` | Docker Postgres user | `postgres` |
| `POSTGRES_PASSWORD` | Docker Postgres password | `secret` |

---

## API Reference

All endpoints are prefixed with `/api`.

### Account Management

#### Create Personal Account

```
POST /api/create-personal-account
```

**Request Body:**

```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane.doe@example.com",
  "password": "securePassword123",
  "phoneNumber": "08012345678",
  "gender": "FEMALE",
  "dateOfBirth": "1995-06-15",
  "address": "12 Lagos Street, Abuja",
  "nin": "12345678901",
  "bvn": "98765432100"
}
```

**Response `200 OK`:**

```json
{
  "data": {
    "accountNumber": "2026847291"
  },
  "message": "Account Creation Successful",
  "statusCode": "201 CREATED"
}
```

**Validation rules:**
- `phoneNumber` must match Nigerian format: `0[7|8|9][0|1][0-9]{8}`
- `dateOfBirth` — customer must be at least 18 years old
- `email` must be unique across all customers
- `phoneNumber` must be unique across all customers

---

### Transaction Management

#### Transfer Funds

```
POST /api/transaction/transfer
```

**Request Body:**

```json
{
  "accountId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "amount": 5000.00,
  "destinationAccount": "2026847291",
  "description": "Monthly rent payment"
}
```

**Response `201 CREATED`:**

```json
{
  "data": {
    "transactionStatus": "SUCCESSFUL"
  },
  "message": "Transaction successful",
  "statusCode": "201 CREATED"
}
```

**Business rules:**
- Both source and destination accounts must exist
- Source account must have sufficient balance
- Creates DEBIT (source) and CREDIT (destination) ledger entries, both `SETTLED`

---

#### Deposit Funds (Card)

```
POST /api/transaction/deposit
```

**Request Body:**

```json
{
  "accountId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "cardNumber": "7893234572819472",
  "cardName": "SOLOMON GRUNDY",
  "dateOfExpiry": "2029-01",
  "cvc": 324,
  "depositAmount": 10000.00,
  "description": "Wallet top-up"
}
```

**Response `201 CREATED`** (successful card):

```json
{
  "data": { "transactionStatus": "SUCCESSFUL" },
  "message": "Deposit Successful",
  "statusCode": "201 CREATED"
}
```

**Response `202 ACCEPTED`** (pending card):

```json
{
  "data": { "transactionStatus": "PENDING" },
  "message": "Deposit Successful",
  "statusCode": "202 ACCEPTED"
}
```

**Business rules:**
- Minimum deposit amount: **100**
- Card number, name, expiry, and CVC must all match the card on record
- Transaction status is determined by the card's configured status

**Test cards (available in dev/staging):**

| Card Number | Name | Expiry | CVC | Outcome |
|---|---|---|---|---|
| `7893234572819472` | SOLOMON GRUNDY | 2029-01 | 324 | SUCCESSFUL |
| `1234567893824913` | CHIOMA PRECIOUS | 2027-08 | 372 | PENDING |

---

#### Requery Transaction

```
PUT /api/transaction/requery/{transaction-id}
```

**Path Parameter:** `transaction-id` — UUID of the pending transaction

**Response `200 OK`:**

```json
{
  "data": { "transactionStatus": "SUCCESSFUL" },
  "message": "Transaction Successful",
  "statusCode": "201 CREATED"
}
```

**Business rules:**
- Transaction must exist
- Transaction must be in `PENDING` status
- Outcome is randomly resolved to `SUCCESSFUL` or `DECLINED`
- If `SUCCESSFUL`: account is credited and ledger entries are `SETTLED`
- If `DECLINED`: ledger entries are `VOID`

---

### Error Responses

All errors follow a consistent format:

```json
{
  "message": "Insufficient funds available in account",
  "statusCode": 400
}
```

| HTTP Status | Scenario |
|---|---|
| `400 Bad Request` | Validation failure, insufficient funds, underage, duplicate, non-pending requery |
| `404 Not Found` | Account, card, or transaction not found |

---

## Data Model

```
users (base table)
 ├── customers (NIN, BVN, address — JOINED inheritance)
 │    └── accounts (1:1 per customer)
 │         └── ledger_entries (M:1 per transaction)
 │              └── transactions (DEPOSIT | WITHDRAWAL | TRANSFER)
 └── (Business — separate entity, not extending User)
```

### Account Tiers

| Tier | Description |
|---|---|
| `TIER_1` | Default tier on registration |
| `TIER_2` | Upgraded tier |
| `TIER_3` | Premium tier |

### Account Statuses

| Status | Description |
|---|---|
| `ACTIVE` | Account is operational |
| `DORMANT` | Account is inactive |
| `FROZEN` | Account is locked |

### Transaction Statuses

| Status | Description |
|---|---|
| `PENDING` | Awaiting payment gateway confirmation |
| `SUCCESSFUL` | Transaction completed |
| `DECLINED` | Transaction rejected |

---

## Testing

Run the full test suite:

```bash
mvn test
```

Run a specific test class:

```bash
mvn test -Dtest=CustomerServiceImplTest
```

**Test coverage by layer:**

| Layer | Test Class | Type |
|---|---|---|
| Controller | `AccountControllerTest` | MockMvc / Integration |
| Controller | `TransactionControllerTest` | MockMvc / Integration |
| Service | `CustomerServiceImplTest` | Unit (Mockito) |
| Service | `TransactionServiceImplTest` | Unit (Mockito + MockedStatic) |
| Service | `CardDepositServiceImplTest` | Unit (Mockito) |
| Utility | `AccountUtilTest` | Unit |
| Utility | `TransactionRequeryUtilTest` | Unit |

---

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feat/your-feature`)
3. Commit your changes following the existing commit style
4. Push the branch and open a pull request against `master`
5. Ensure all tests pass before requesting review

---

## Group A — NAIJUG Bootcamp Final Project

Built with Java 21 + Spring Boot 4.1
