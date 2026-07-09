package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.request.CardDetailsRequest;
import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.LedgerEntry;
import com.groupa.digitalbackendapplication.domain.entities.Transaction;
import com.groupa.digitalbackendapplication.domain.enums.EntryType;
import com.groupa.digitalbackendapplication.domain.enums.LedgerEntryStatus;
import com.groupa.digitalbackendapplication.domain.enums.TransactionStatus;
import com.groupa.digitalbackendapplication.domain.enums.TransactionType;
import com.groupa.digitalbackendapplication.repository.TransactionRepository;
import com.groupa.digitalbackendapplication.service.DepositService;
import com.groupa.digitalbackendapplication.utils.TransactionUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Validated
@RequiredArgsConstructor

public class CardDepositServiceImpl implements DepositService {
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public Transaction buildSuccessfulDeposit(Account account, @Valid CardDetailsRequest payload) {
        BigDecimal depositAmount = payload.depositAmount();

        Transaction transaction = TransactionUtil.buildTransactionEntity(TransactionType.DEPOSIT, TransactionStatus.SUCCESSFUL,
                null, account, depositAmount, payload.description());

        //Fund account
        account.setBalance(account.getBalance().add(depositAmount));

        LocalDateTime now = LocalDateTime.now();

        LedgerEntry debitEntry = new LedgerEntry();
        debitEntry.setAccount(null);
        debitEntry.setEntryType(EntryType.DEBIT);
        debitEntry.setStatus(LedgerEntryStatus.SETTLED);
        debitEntry.setAmount(depositAmount);
        debitEntry.setSettledAt(now);

        LedgerEntry creditEntry = new LedgerEntry();
        creditEntry.setAccount(account);
        creditEntry.setStatus(LedgerEntryStatus.SETTLED);
        creditEntry.setEntryType(EntryType.CREDIT);
        creditEntry.setAmount(depositAmount);
        creditEntry.setSettledAt(now);

        //Save transaction to db
        transaction.addLedger(debitEntry);
        transaction.addLedger(creditEntry);
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction buildPendingDeposit(Account account, CardDetailsRequest payload) {
        BigDecimal depositAmount = payload.depositAmount();

        Transaction transaction = TransactionUtil.buildTransactionEntity(TransactionType.DEPOSIT, TransactionStatus.PENDING,
                null, account, depositAmount, payload.description().trim());

        LedgerEntry debitEntry = new LedgerEntry();
        debitEntry.setAccount(null);
        debitEntry.setStatus(LedgerEntryStatus.PENDING);
        debitEntry.setEntryType(EntryType.DEBIT);
        debitEntry.setAmount(depositAmount);

        LedgerEntry creditEntry = new LedgerEntry();
        creditEntry.setAccount(account);
        creditEntry.setStatus(LedgerEntryStatus.PENDING);
        creditEntry.setEntryType(EntryType.CREDIT);
        creditEntry.setAmount(depositAmount);

        transaction.addLedger(debitEntry);
        transaction.addLedger(creditEntry);

        return transactionRepository.save(transaction);
    }
}
