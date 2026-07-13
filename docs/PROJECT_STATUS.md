# Project Status — Digital Banking Application

> Last updated: 2026-07-13
> Project: NAIJUG Bootcamp Final Project — Group A

This document tracks everything that has been built, what is in progress, and what remains to be built across both the backend and the planned frontend.

---

## Backend

### Built and Merged

#### Customer & Account Management
- [x] `User` base entity with JOINED inheritance strategy (`users` table)
- [x] `Customer` entity extending `User` — stores `address`, `NIN`, `BVN` (`customers` table)
- [x] `Account` entity — `accountNumber`, `balance`, `accountStatus`, `accountTier`, `ownerId` (`accounts` table)
- [x] `POST /api/create-personal-account` — full registration flow:
  - Age validation (minimum 18 years)
  - Duplicate phone number check
  - Duplicate email check
  - Nigerian phone number regex validation
  - Account number generation (`AccountUtil` — year + 6-digit random)
  - Account provisioned at `TIER_1 / ACTIVE / balance = 0`

#### Fund Transfers
- [x] `Transaction` entity — `transactionType`, `transactionStatus`, `sourceAccount`, `destinationAccount`, `amountTransferred`, `description` (`transactions` table)
- [x] `LedgerEntry` entity — double-entry bookkeeping with `DEBIT / CREDIT`, `SETTLED / PENDING / VOID` statuses (`ledger_entry` table)
- [x] `POST /api/transaction/transfer` — fund transfer flow:
  - Source account existence check
  - Destination account existence check by account number
  - Sufficient balance check
  - Debit source account, credit destination account
  - Two ledger entries created (DEBIT + CREDIT, both `SETTLED`)
  - Full `@Transactional` wrapping

#### Card Deposits
- [x] `CardDetails` record — `cardNumber`, `cardName`, `dateOfExpiry`, `cvc`, `transactionStatus`
- [x] `CardDetailsRepository` — in-memory mock with two pre-loaded test cards (SUCCESSFUL + PENDING)
- [x] `POST /api/transaction/deposit` — card deposit flow:
  - Minimum deposit amount guard (≥ 100)
  - Account existence check
  - Card lookup by card number
  - Full card detail verification (name, expiry, CVC)
  - Routes to `buildSuccessfulDeposit` or `buildPendingDeposit` based on card status
  - Returns `201 CREATED` for successful deposits, `202 ACCEPTED` for pending

#### Transaction Requery
- [x] `PUT /api/transaction/requery/{transaction-id}` — payment reconciliation simulation:
  - Transaction existence check
  - Status guard (only `PENDING` transactions can be requeried)
  - Random `SUCCESSFUL` / `DECLINED` outcome via `TransactionRequeryUtil`
  - If `SUCCESSFUL`: credits destination account, settles all ledger entries
  - If `DECLINED`: voids all ledger entries, no balance change

#### Infrastructure & Cross-Cutting
- [x] `GlobalExceptionHandler` (`@ControllerAdvice`) for `BadRequestException` (400) and `ResourceNotFoundException` (404)
- [x] `ResponseWrapper<T>` — generic consistent response envelope (`data`, `message`, `statusCode`)
- [x] Docker Compose setup — PostgreSQL 16 on port 5433 + Adminer on port 8082
- [x] HikariCP connection pool configured (max 10 connections)
- [x] Jakarta Bean Validation on all request DTOs

#### Supporting Entities (Scaffolded — not yet wired to endpoints)
- [x] `Business` entity — `businessName`, `businessAddress`, `cacNumber`, `businessEmail`, `accountNumber`
- [x] `KycEntity` — `customerId`, `bvn`, `nin`, `submissionStatus`
- [x] `AuditLog` — `userId`, `userEmail`, `actionType`, `entityType`
- [x] `ActionType` enum — placeholder, no values yet
- [x] `SubmissionStatus` enum — placeholder, no values yet

#### Testing
- [x] `CustomerServiceImplTest` — 5 unit tests (success, underage, duplicate phone, duplicate email, exactly 18)
- [x] `TransactionServiceImplTest` — 10 unit tests (all transfer, deposit, and requery scenarios)
- [x] `CardDepositServiceImplTest` — 6 unit tests (successful/pending deposits, balance updates, ledger entries)
- [x] `AccountControllerTest` — 6 MockMvc tests (success + all validation failure paths)
- [x] `TransactionControllerTest` — 9 MockMvc tests (all endpoints, success + error paths)
- [x] `AccountUtilTest` — 4 unit tests (length, year prefix, numeric format, range)
- [x] `TransactionRequeryUtilTest` — 4 unit tests (non-blank, valid enum, only 2 values, parseable)

---

### Remaining Backend Work

#### Security & Authentication
- [ ] Spring Security integration
- [ ] JWT-based authentication (`POST /api/auth/login`, `POST /api/auth/register`)
- [ ] Role-based access control — `ADMIN`, `EMPLOYEE`, `CUSTOMER` roles are defined but not enforced
- [ ] Password hashing (BCrypt) — passwords are currently stored in plain text
- [ ] JWT token refresh endpoint
- [ ] Logout / token invalidation

#### Business Account Management
- [ ] `POST /api/create-business-account` — register a business account
  - CAC number validation (`[RC|BN|IT|LP] [0-9]{6}`)
  - Unique business email, CAC number, business name
- [ ] Business account endpoints (transfer, deposit, balance check)

#### Account Upgrade
- [ ] `UpgradeRequest` DTO exists but has no endpoint
- [ ] `PUT /api/account/upgrade` — upgrade account tier (TIER_1 → TIER_2 → TIER_3)
- [ ] Tier-based transaction limits

#### KYC Verification
- [ ] `KycEntity` exists but has no endpoint or service
- [ ] `POST /api/kyc/submit` — submit NIN/BVN for verification
- [ ] `GET /api/kyc/status` — check KYC verification status
- [ ] Auto-link KYC to account tier upgrades

#### Audit Log
- [ ] `AuditLog` entity exists but is not populated anywhere
- [ ] Hook audit logging into all state-changing operations (account creation, transfers, upgrades)
- [ ] `GET /api/admin/audit-logs` — admin endpoint for audit trail

#### Transaction History
- [ ] `GET /api/account/{id}/transactions` — paginated transaction history for an account
- [ ] `GET /api/transaction/{id}` — fetch single transaction details
- [ ] Filtering by date range, status, type

#### Account Management Endpoints
- [ ] `GET /api/account/{id}` — fetch account details (balance, tier, status)
- [ ] `PUT /api/account/{id}/freeze` — freeze an account (admin)
- [ ] `PUT /api/account/{id}/unfreeze` — unfreeze an account (admin)
- [ ] Account balance enquiry endpoint

#### Withdrawal
- [ ] `POST /api/transaction/withdraw` — withdraw from account (currently no withdrawal endpoint despite `WITHDRAWAL` type in enum)

#### Notifications
- [ ] Email notification on successful account creation
- [ ] Email/SMS notification on transactions
- [ ] Integration with an email service (e.g., JavaMail, SendGrid)

#### Real Card Scheme Integration
- [ ] Replace in-memory `CardDetailsRepository` with a real card scheme adapter
- [ ] Webhook endpoint for async payment gateway callbacks (instead of synchronous requery)

#### Production Hardening
- [ ] Input sanitization and SQL injection protection audit
- [ ] Rate limiting on sensitive endpoints (login, transfer)
- [ ] API versioning strategy (`/api/v1/...`)
- [ ] Swagger / OpenAPI documentation (`springdoc-openapi`)
- [ ] Flyway or Liquibase database migration management
- [ ] Structured logging (JSON log format)
- [ ] Health check endpoint (`/actuator/health`)
- [ ] Spring Boot Actuator integration

#### Testing Gaps
- [ ] Repository integration tests using `@DataJpaTest` with H2
- [ ] End-to-end integration tests using `@SpringBootTest` + TestContainers (PostgreSQL)
- [ ] Validation constraint tests (custom annotation coverage)
- [ ] Concurrency/race condition tests for simultaneous transfers

---

## Frontend

### Status: Not Yet Started

No frontend codebase exists at this time. The backend exposes a REST API that a frontend client will consume.

---

### Recommended Frontend Stack

| Concern | Recommended Technology |
|---|---|
| Framework | React 19 (with TypeScript) |
| Routing | React Router v7 |
| State Management | Zustand or React Query (TanStack Query) |
| UI Component Library | shadcn/ui or Tailwind CSS |
| HTTP Client | Axios or native Fetch with React Query |
| Form Handling | React Hook Form + Zod |
| Build Tool | Vite |
| Testing | Vitest + React Testing Library |

---

### Frontend Pages / Features to Build

#### Authentication
- [ ] Login page (email + password)
- [ ] Registration page — mirrors `POST /api/create-personal-account`
- [ ] JWT storage and refresh token management
- [ ] Protected route guards

#### Customer Dashboard
- [ ] Account summary card (account number, balance, tier, status)
- [ ] Recent transactions list (paginated)
- [ ] Quick-action buttons (Transfer, Deposit, Requery)

#### Fund Transfer
- [ ] Transfer form (source account, destination account number, amount, description)
- [ ] Real-time balance display
- [ ] Transfer confirmation modal
- [ ] Success / failure feedback with transaction status

#### Card Deposit
- [ ] Card detail input form (card number, name, expiry, CVC, amount)
- [ ] Deposit amount validation (minimum 100)
- [ ] PENDING state handling — show "processing" state to user
- [ ] Success / pending / failure feedback screens

#### Transaction Requery
- [ ] Pending transactions list with requery button
- [ ] Requery loading state
- [ ] Updated status display after requery resolves

#### Account Management
- [ ] Account details page (tier, status, full account info)
- [ ] KYC submission form (NIN, BVN upload)
- [ ] Account tier upgrade request

#### Admin Panel (Future)
- [ ] Audit log viewer
- [ ] Account freeze / unfreeze controls
- [ ] User management

---

## Summary

| Area | Status |
|---|---|
| Core Backend API | Functional |
| Security / Auth | Not started |
| Business Accounts | Scaffolded — no endpoints |
| KYC | Scaffolded — no endpoints |
| Audit Logging | Scaffolded — not wired |
| Transaction History | Not started |
| Withdrawal | Not started |
| Notifications | Not started |
| API Docs (Swagger) | Not started |
| DB Migrations | Not started |
| Unit & Controller Tests | Complete |
| Integration Tests | Not started |
| Frontend | Not started |
