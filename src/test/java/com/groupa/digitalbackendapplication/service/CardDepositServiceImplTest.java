package com.groupa.digitalbackendapplication.service;

import com.groupa.digitalbackendapplication.domain.dto.request.CardDetailsRequest;
import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.LedgerEntry;
import com.groupa.digitalbackendapplication.domain.entities.Transaction;
import com.groupa.digitalbackendapplication.domain.enums.*;
import com.groupa.digitalbackendapplication.repository.TransactionRepository;
import com.groupa.digitalbackendapplication.service.impl.CardDepositServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardDepositServiceImpl Unit Tests")
class CardDepositServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CardDepositServiceImpl cardDepositService;

    private Account account;
    private CardDetailsRequest cardRequest;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(UUID.randomUUID())
                .accountNumber("2026333333")
                .balance(BigDecimal.valueOf(1_000))
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        cardRequest = new CardDetailsRequest(
                account.getId(),
                "7893234572819472",
                "SOLOMON GRUNDY",
                YearMonth.of(2029, 1),
                324,
                BigDecimal.valueOf(5_000),
                "Card top-up"
        );
    }

    @Test
    @DisplayName("buildSuccessfulDeposit: should set transaction status to SUCCESSFUL")
    void buildSuccessfulDeposit_TransactionIsSuccessful() {
        Transaction expected = Transaction.builder()
                .transactionStatus(TransactionStatus.SUCCESSFUL)
                .ledgerEntries(new ArrayList<>())
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(expected);

        Transaction result = cardDepositService.buildSuccessfulDeposit(account, cardRequest);

        assertThat(result.getTransactionStatus()).isEqualTo(TransactionStatus.SUCCESSFUL);
    }

    @Test
    @DisplayName("buildSuccessfulDeposit: should credit the account balance")
    void buildSuccessfulDeposit_CreditsAccountBalance() {
        BigDecimal initialBalance = account.getBalance();
        BigDecimal depositAmount = cardRequest.depositAmount();

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        cardDepositService.buildSuccessfulDeposit(account, cardRequest);

        assertThat(account.getBalance()).isEqualByComparingTo(initialBalance.add(depositAmount));
    }

    @Test
    @DisplayName("buildSuccessfulDeposit: should create SETTLED ledger entries with correct types")
    void buildSuccessfulDeposit_CreatesSettledLedgerEntries() {
        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        when(transactionRepository.save(txCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        cardDepositService.buildSuccessfulDeposit(account, cardRequest);

        Transaction captured = txCaptor.getValue();
        assertThat(captured.getLedgerEntries()).hasSize(2);

        LedgerEntry debit = captured.getLedgerEntries().stream()
                .filter(e -> e.getEntryType() == EntryType.DEBIT)
                .findFirst().orElseThrow();
        LedgerEntry credit = captured.getLedgerEntries().stream()
                .filter(e -> e.getEntryType() == EntryType.CREDIT)
                .findFirst().orElseThrow();

        assertThat(debit.getStatus()).isEqualTo(LedgerEntryStatus.SETTLED);
        assertThat(credit.getStatus()).isEqualTo(LedgerEntryStatus.SETTLED);
        assertThat(credit.getAccount()).isEqualTo(account);
        assertThat(debit.getAccount()).isNull();
    }

    @Test
    @DisplayName("buildPendingDeposit: should set transaction status to PENDING")
    void buildPendingDeposit_TransactionIsPending() {
        Transaction expected = Transaction.builder()
                .transactionStatus(TransactionStatus.PENDING)
                .ledgerEntries(new ArrayList<>())
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(expected);

        Transaction result = cardDepositService.buildPendingDeposit(account, cardRequest);

        assertThat(result.getTransactionStatus()).isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    @DisplayName("buildPendingDeposit: should NOT credit the account balance")
    void buildPendingDeposit_DoesNotCreditAccountBalance() {
        BigDecimal initialBalance = account.getBalance();

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        cardDepositService.buildPendingDeposit(account, cardRequest);

        assertThat(account.getBalance()).isEqualByComparingTo(initialBalance);
    }

    @Test
    @DisplayName("buildPendingDeposit: should create PENDING ledger entries")
    void buildPendingDeposit_CreatesPendingLedgerEntries() {
        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        when(transactionRepository.save(txCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        cardDepositService.buildPendingDeposit(account, cardRequest);

        Transaction captured = txCaptor.getValue();
        assertThat(captured.getLedgerEntries()).hasSize(2);
        assertThat(captured.getLedgerEntries())
                .allMatch(e -> e.getStatus() == LedgerEntryStatus.PENDING);
    }

    @Test
    @DisplayName("buildPendingDeposit: should persist the transaction via repository")
    void buildPendingDeposit_SavesTransaction() {
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        cardDepositService.buildPendingDeposit(account, cardRequest);

        verify(transactionRepository).save(any(Transaction.class));
    }
}
