package com.groupa.digitalbackendapplication.service;

import com.groupa.digitalbackendapplication.domain.dto.request.CardDetailsRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.TransferFundsRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.dto.response.TransactionResponse;
import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.CardDetails;
import com.groupa.digitalbackendapplication.domain.entities.LedgerEntry;
import com.groupa.digitalbackendapplication.domain.entities.Transaction;
import com.groupa.digitalbackendapplication.domain.enums.*;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.exceptions.ResourceNotFoundException;
import com.groupa.digitalbackendapplication.repository.AccountRepository;
import com.groupa.digitalbackendapplication.repository.CardDetailsRepository;
import com.groupa.digitalbackendapplication.repository.TransactionRepository;
import com.groupa.digitalbackendapplication.service.impl.TransactionServiceImpl;
import com.groupa.digitalbackendapplication.utils.TransactionRequeryUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionServiceImpl Unit Tests")
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardDetailsRepository cardDetailsRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private DepositService depositService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Account sourceAccount;
    private Account destinationAccount;
    private Transaction savedTransaction;

    @BeforeEach
    void setUp() {
        sourceAccount = Account.builder()
                .id(UUID.randomUUID())
                .accountNumber("2026111111")
                .balance(BigDecimal.valueOf(10_000))
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        destinationAccount = Account.builder()
                .id(UUID.randomUUID())
                .accountNumber("2026222222")
                .balance(BigDecimal.ZERO)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        savedTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionType(TransactionType.TRANSFER)
                .transactionStatus(TransactionStatus.SUCCESSFUL)
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .amountTransferred(BigDecimal.valueOf(1_000))
                .description("Test transfer")
                .ledgerEntries(new ArrayList<>())
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // transferFunds
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Transfer: should successfully transfer funds between two valid accounts")
    void transferFunds_Success() {
        TransferFundsRequest request = new TransferFundsRequest(
                sourceAccount.getId(), BigDecimal.valueOf(1_000), "2026222222", "Rent payment");

        when(accountRepository.findById(sourceAccount.getId())).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber("2026222222")).thenReturn(Optional.of(destinationAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        ResponseWrapper<TransactionResponse> response = transactionService.transferFunds(request);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Transaction successful");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getData().status()).isEqualTo(TransactionStatus.SUCCESSFUL);
    }

    @Test
    @DisplayName("Transfer: should throw ResourceNotFoundException when source account does not exist")
    void transferFunds_SourceAccountNotFound_ThrowsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        TransferFundsRequest request = new TransferFundsRequest(
                unknownId, BigDecimal.valueOf(500), "2026222222", "Payment");

        when(accountRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.transferFunds(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account not found");
    }

    @Test
    @DisplayName("Transfer: should throw ResourceNotFoundException when destination account does not exist")
    void transferFunds_DestinationAccountNotFound_ThrowsResourceNotFoundException() {
        TransferFundsRequest request = new TransferFundsRequest(
                sourceAccount.getId(), BigDecimal.valueOf(500), "9999999999", "Payment");

        when(accountRepository.findById(sourceAccount.getId())).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber("9999999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.transferFunds(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account number does not exist");
    }

    @Test
    @DisplayName("Transfer: should throw BadRequestException when source account has insufficient funds")
    void transferFunds_InsufficientFunds_ThrowsBadRequestException() {
        TransferFundsRequest request = new TransferFundsRequest(
                sourceAccount.getId(), BigDecimal.valueOf(50_000), "2026222222", "Too much");

        when(accountRepository.findById(sourceAccount.getId())).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber("2026222222")).thenReturn(Optional.of(destinationAccount));

        assertThatThrownBy(() -> transactionService.transferFunds(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    @DisplayName("Transfer: source balance decreases and destination balance increases after transfer")
    void transferFunds_BalancesAreUpdatedCorrectly() {
        BigDecimal transferAmount = BigDecimal.valueOf(3_000);
        TransferFundsRequest request = new TransferFundsRequest(
                sourceAccount.getId(), transferAmount, "2026222222", "Salary");

        when(accountRepository.findById(sourceAccount.getId())).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber("2026222222")).thenReturn(Optional.of(destinationAccount));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        transactionService.transferFunds(request);

        assertThat(sourceAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(7_000));
        assertThat(destinationAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(3_000));
    }

    // ─────────────────────────────────────────────────────────────
    // depositFunds
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deposit: should return CREATED (201) for a card with SUCCESSFUL transaction status")
    void depositFunds_SuccessfulCard_Returns201() {
        CardDetails successCard = new CardDetails(
                "7893234572819472", "SOLOMON GRUNDY", YearMonth.of(2029, 1), 324, TransactionStatus.SUCCESSFUL);

        CardDetailsRequest request = new CardDetailsRequest(
                destinationAccount.getId(), "7893234572819472", "SOLOMON GRUNDY",
                YearMonth.of(2029, 1), 324, BigDecimal.valueOf(5_000), "Top-up");

        Transaction successTx = Transaction.builder()
                .transactionStatus(TransactionStatus.SUCCESSFUL)
                .ledgerEntries(new ArrayList<>())
                .build();

        when(accountRepository.findById(destinationAccount.getId())).thenReturn(Optional.of(destinationAccount));
        when(cardDetailsRepository.findByCardNumber("7893234572819472")).thenReturn(Optional.of(successCard));
        when(depositService.buildSuccessfulDeposit(any(), any())).thenReturn(successTx);

        ResponseWrapper<TransactionResponse> response = transactionService.depositFunds(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getMessage()).isEqualTo("Deposit Successful");
        assertThat(response.getData().status()).isEqualTo(TransactionStatus.SUCCESSFUL);
    }

    @Test
    @DisplayName("Deposit: should return ACCEPTED (202) for a card with PENDING transaction status")
    void depositFunds_PendingCard_Returns202() {
        CardDetails pendingCard = new CardDetails(
                "1234567893824913", "CHIOMA PRECIOUS", YearMonth.of(2027, 8), 372, TransactionStatus.PENDING);

        CardDetailsRequest request = new CardDetailsRequest(
                destinationAccount.getId(), "1234567893824913", "CHIOMA PRECIOUS",
                YearMonth.of(2027, 8), 372, BigDecimal.valueOf(2_000), "Top-up");

        Transaction pendingTx = Transaction.builder()
                .transactionStatus(TransactionStatus.PENDING)
                .ledgerEntries(new ArrayList<>())
                .build();

        when(accountRepository.findById(destinationAccount.getId())).thenReturn(Optional.of(destinationAccount));
        when(cardDetailsRepository.findByCardNumber("1234567893824913")).thenReturn(Optional.of(pendingCard));
        when(depositService.buildPendingDeposit(any(), any())).thenReturn(pendingTx);

        ResponseWrapper<TransactionResponse> response = transactionService.depositFunds(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getData().status()).isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    @DisplayName("Deposit: should throw BadRequestException when deposit amount is below minimum (100)")
    void depositFunds_AmountBelowMinimum_ThrowsBadRequestException() {
        CardDetailsRequest request = new CardDetailsRequest(
                destinationAccount.getId(), "7893234572819472", "SOLOMON GRUNDY",
                YearMonth.of(2029, 1), 324, BigDecimal.valueOf(50), "Low amount");

        assertThatThrownBy(() -> transactionService.depositFunds(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("less than 100");
    }

    @Test
    @DisplayName("Deposit: should throw ResourceNotFoundException when account does not exist")
    void depositFunds_AccountNotFound_ThrowsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        CardDetailsRequest request = new CardDetailsRequest(
                unknownId, "7893234572819472", "SOLOMON GRUNDY",
                YearMonth.of(2029, 1), 324, BigDecimal.valueOf(1_000), "Deposit");

        when(accountRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.depositFunds(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account not found");
    }

    @Test
    @DisplayName("Deposit: should throw ResourceNotFoundException when card number does not exist")
    void depositFunds_CardNotFound_ThrowsResourceNotFoundException() {
        CardDetailsRequest request = new CardDetailsRequest(
                destinationAccount.getId(), "0000000000000000", "UNKNOWN PERSON",
                YearMonth.of(2029, 1), 999, BigDecimal.valueOf(1_000), "Deposit");

        when(accountRepository.findById(destinationAccount.getId())).thenReturn(Optional.of(destinationAccount));
        when(cardDetailsRepository.findByCardNumber("0000000000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.depositFunds(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Invalid card");
    }

    @Test
    @DisplayName("Deposit: should throw BadRequestException when card details do not match")
    void depositFunds_CardDetailsMismatch_ThrowsBadRequestException() {
        CardDetails storedCard = new CardDetails(
                "7893234572819472", "SOLOMON GRUNDY", YearMonth.of(2029, 1), 324, TransactionStatus.SUCCESSFUL);

        CardDetailsRequest requestWithWrongName = new CardDetailsRequest(
                destinationAccount.getId(), "7893234572819472", "WRONG NAME",
                YearMonth.of(2029, 1), 324, BigDecimal.valueOf(1_000), "Deposit");

        when(accountRepository.findById(destinationAccount.getId())).thenReturn(Optional.of(destinationAccount));
        when(cardDetailsRepository.findByCardNumber("7893234572819472")).thenReturn(Optional.of(storedCard));

        assertThatThrownBy(() -> transactionService.depositFunds(requestWithWrongName))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("card details");
    }

    // ─────────────────────────────────────────────────────────────
    // requeryTransaction
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Requery: should throw ResourceNotFoundException when transaction does not exist")
    void requeryTransaction_NotFound_ThrowsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(transactionRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.requeryTransaction(unknownId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction does not exist");
    }

    @Test
    @DisplayName("Requery: should throw BadRequestException when transaction is not in PENDING status")
    void requeryTransaction_NotPending_ThrowsBadRequestException() {
        Transaction completedTx = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionStatus(TransactionStatus.SUCCESSFUL)
                .ledgerEntries(new ArrayList<>())
                .build();

        when(transactionRepository.findById(completedTx.getId())).thenReturn(Optional.of(completedTx));

        assertThatThrownBy(() -> transactionService.requeryTransaction(completedTx.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot requery a transaction that is not pending");
    }

    @Test
    @DisplayName("Requery: should credit account and settle ledger entries when outcome is SUCCESSFUL")
    void requeryTransaction_OutcomeSuccessful_CreditsAccountAndSettlesLedger() {
        LedgerEntry pendingCredit = new LedgerEntry();
        pendingCredit.setStatus(LedgerEntryStatus.PENDING);
        pendingCredit.setEntryType(EntryType.CREDIT);
        pendingCredit.setAmount(BigDecimal.valueOf(500));

        Transaction pendingTx = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionStatus(TransactionStatus.PENDING)
                .destinationAccount(destinationAccount)
                .amountTransferred(BigDecimal.valueOf(500))
                .ledgerEntries(new ArrayList<>(List.of(pendingCredit)))
                .build();
        pendingCredit.setTransaction(pendingTx);

        when(transactionRepository.findById(pendingTx.getId())).thenReturn(Optional.of(pendingTx));
        when(transactionRepository.save(any())).thenReturn(pendingTx);

        try (MockedStatic<TransactionRequeryUtil> mockedUtil = mockStatic(TransactionRequeryUtil.class)) {
            mockedUtil.when(TransactionRequeryUtil::requeryTransactionStatus).thenReturn("SUCCESSFUL");

            ResponseWrapper<TransactionResponse> response = transactionService.requeryTransaction(pendingTx.getId());

            assertThat(response.getData().status()).isEqualTo(TransactionStatus.SUCCESSFUL);
            assertThat(response.getMessage()).isEqualTo("Transaction Successful");
            assertThat(destinationAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(500));
            assertThat(pendingCredit.getStatus()).isEqualTo(LedgerEntryStatus.SETTLED);
            assertThat(pendingCredit.getSettledAt()).isNotNull();
        }
    }

    @Test
    @DisplayName("Requery: should void ledger entries when outcome is DECLINED")
    void requeryTransaction_OutcomeDeclined_VoidsLedgerEntries() {
        LedgerEntry pendingCredit = new LedgerEntry();
        pendingCredit.setStatus(LedgerEntryStatus.PENDING);
        pendingCredit.setEntryType(EntryType.CREDIT);
        pendingCredit.setAmount(BigDecimal.valueOf(500));

        Transaction pendingTx = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionStatus(TransactionStatus.PENDING)
                .destinationAccount(destinationAccount)
                .amountTransferred(BigDecimal.valueOf(500))
                .ledgerEntries(new ArrayList<>(List.of(pendingCredit)))
                .build();
        pendingCredit.setTransaction(pendingTx);

        BigDecimal balanceBefore = destinationAccount.getBalance();

        when(transactionRepository.findById(pendingTx.getId())).thenReturn(Optional.of(pendingTx));
        when(transactionRepository.save(any())).thenReturn(pendingTx);

        try (MockedStatic<TransactionRequeryUtil> mockedUtil = mockStatic(TransactionRequeryUtil.class)) {
            mockedUtil.when(TransactionRequeryUtil::requeryTransactionStatus).thenReturn("DECLINED");

            ResponseWrapper<TransactionResponse> response = transactionService.requeryTransaction(pendingTx.getId());

            assertThat(response.getData().status()).isEqualTo(TransactionStatus.DECLINED);
            assertThat(response.getMessage()).isEqualTo("Transaction Failed");
            assertThat(destinationAccount.getBalance()).isEqualByComparingTo(balanceBefore);
            assertThat(pendingCredit.getStatus()).isEqualTo(LedgerEntryStatus.VOID);
            assertThat(pendingCredit.getVoidedAt()).isNotNull();
        }
    }
}
