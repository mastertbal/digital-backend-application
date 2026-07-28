package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.request.CardDetailsRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.TransferFundsRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.dto.response.TransactionResponse;
import com.groupa.digitalbackendapplication.domain.entities.*;
import com.groupa.digitalbackendapplication.domain.enums.*;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.exceptions.ResourceNotFoundException;
import com.groupa.digitalbackendapplication.notification.EmailDetails;
import com.groupa.digitalbackendapplication.notification.EmailService;
import com.groupa.digitalbackendapplication.repository.AccountRepository;
import com.groupa.digitalbackendapplication.repository.CardDetailsRepository;
import com.groupa.digitalbackendapplication.repository.CustomerRepository;
import com.groupa.digitalbackendapplication.repository.TransactionRepository;
import com.groupa.digitalbackendapplication.service.DepositService;
import com.groupa.digitalbackendapplication.service.TransactionService;
import com.groupa.digitalbackendapplication.utils.TransactionRequeryUtil;
import com.groupa.digitalbackendapplication.utils.TransactionUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Validated
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardDetailsRepository cardDetailsRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final DepositService depositService;
    private final EmailService emailService;

    @Override
    @Transactional
    public ResponseWrapper<TransactionResponse> transferFunds(@Valid TransferFundsRequest payload) {
        Account sourceAccount= accountRepository.findById(payload.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        //Does destination Account exists
        Account destinationAccount = accountRepository.findByAccountNumber(payload.destinationAccount().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Account number does not exist"));


        if(payload.amount().compareTo(sourceAccount.getBalance()) > 0)
            throw new BadRequestException("Insufficient funds available in account");

        Transaction transaction = TransactionUtil.buildTransactionEntity(TransactionType.TRANSFER, TransactionStatus.SUCCESSFUL,sourceAccount,
                destinationAccount, payload.amount(), payload.description().trim());

        //Deduct from sender, credit receiver
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(payload.amount()));
        destinationAccount.setBalance(destinationAccount.getBalance().add(payload.amount()));

        //Ledger entry for debit
        LocalDateTime now = LocalDateTime.now();

        LedgerEntry debit = new LedgerEntry();
        debit.setEntryType(EntryType.DEBIT);
        debit.setStatus(LedgerEntryStatus.SETTLED);
        debit.setAccount(sourceAccount);
        debit.setAmount(payload.amount());
        debit.setSettledAt(now);

        //Ledger entry for credit
        LedgerEntry credit = new LedgerEntry();
        credit.setEntryType(EntryType.CREDIT);
        credit.setStatus(LedgerEntryStatus.SETTLED);
        credit.setAccount(destinationAccount);
        credit.setAmount(payload.amount());
        credit.setSettledAt(now);

        //Save transaction to db
        transaction.addLedger(debit);
        transaction.addLedger(credit);
        transaction = transactionRepository.save(transaction);

        //Send debit alert to sender while credit alert to receiver
        sendDebitAlert(sourceAccount, payload.amount(), now);
        sendCreditAlert(destinationAccount, payload.amount(), now);

        return ResponseWrapper.<TransactionResponse>builder()
                .data(buildTransactionResponse(transaction.getTransactionStatus()))
                .message("Transaction successful")
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @Override
    @Transactional
    public ResponseWrapper<TransactionResponse> depositFunds(@Valid CardDetailsRequest payload) {
        if(payload.depositAmount().compareTo(BigDecimal.valueOf(100)) < 0)
            throw new BadRequestException("Deposit amount cannot be less than 100");

        Account destinationAccount = accountRepository.findById(payload.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        String payloadCardNumber = payload.cardNumber().trim();
        String payloadCardName = payload.cardName().trim();

        CardDetails cardDetails = cardDetailsRepository.findByCardNumber(payloadCardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid card. Please use a valid card"));

        if(!cardDetails.cardName()
                .equalsIgnoreCase(payloadCardName) || !cardDetails.dateOfExpiry().equals(payload.dateOfExpiry()) || !cardDetails.cvc().equals(payload.cvc())){
            throw new BadRequestException("Problem occurred. Kindly reconfirm your card details and try again");
        }

        //Transaction will either be successful or pending - based on Card used in CardDetailsRepository
        if(cardDetails.transactionStatus() == TransactionStatus.SUCCESSFUL){
            Transaction savedTransaction = depositService.buildSuccessfulDeposit(destinationAccount, payload);

            sendCreditAlert(destinationAccount, payload.depositAmount(), LocalDateTime.now());

            return ResponseWrapper.<TransactionResponse>builder()
                    .data(buildTransactionResponse(savedTransaction.getTransactionStatus()))
                    .message("Deposit Successful")
                    .statusCode(HttpStatus.CREATED)
                    .build();
        }
        else{
            Transaction savedTransaction = depositService.buildPendingDeposit(destinationAccount, payload);

            return ResponseWrapper.<TransactionResponse>builder()
                    .data(buildTransactionResponse(savedTransaction.getTransactionStatus()))
                    .message("Deposit Successful")
                    .statusCode(HttpStatus.ACCEPTED)
                    .build();
        }
    }

    @Override
    @Transactional
    public ResponseWrapper<TransactionResponse> requeryTransaction(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction does not exist"));

        if(transaction.getTransactionStatus() != TransactionStatus.PENDING)
            throw new BadRequestException("Cannot requery a transaction that is not pending");

        //SIMULATING A REQUERY, WHERE TRANSACTION CAN EITHER BE SUCCESSFUL OR DECLINED.
        transaction.setTransactionStatus(TransactionStatus.valueOf(TransactionRequeryUtil.requeryTransactionStatus()));

        TransactionStatus updatedTransactionStatus = transaction.getTransactionStatus();

        //IF UPDATED TRANSACTION STATUS IS NOW SUCCESSFUL, WE CAN ADD THIS FUNDS TO CUSTOMER'S WALLET
        if(updatedTransactionStatus == TransactionStatus.SUCCESSFUL){
            Account account = transaction.getDestinationAccount();
            account.setBalance(account.getBalance().add(transaction.getAmountTransferred()));

            sendCreditAlert(account, transaction.getAmountTransferred(), LocalDateTime.now());
        }

        List<LedgerEntry> existingLedgerEntries = new ArrayList<>(transaction.getLedgerEntries());
        existingLedgerEntries.forEach(transaction::removeLedger);

        LocalDateTime updatedLedgerTime = LocalDateTime.now();

        //IT IS EITHER GOING TO BE SUCCESSFUL OR DECLINED NOW
        for(LedgerEntry ledgerEntry : existingLedgerEntries){
            if(updatedTransactionStatus == TransactionStatus.SUCCESSFUL){
                ledgerEntry.setStatus(LedgerEntryStatus.SETTLED);
                ledgerEntry.setSettledAt(updatedLedgerTime);
            }else{
                ledgerEntry.setStatus(LedgerEntryStatus.VOID);
                ledgerEntry.setVoidedAt(updatedLedgerTime);
            }

            transaction.addLedger(ledgerEntry);
        }

        transactionRepository.save(transaction);

        return ResponseWrapper.<TransactionResponse>builder()
                .message(updatedTransactionStatus == TransactionStatus.SUCCESSFUL ? "Transaction Successful" : "Transaction Failed")
                .data(buildTransactionResponse(updatedTransactionStatus))
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    private void sendDebitAlert(Account account, BigDecimal amount, LocalDateTime date){
        try {
            Customer customer = getCustomer(account.getOwnerId());

            String message = "Dear " + customer.getFirstName() + ",\n\n" +
                    "Your account has been debited with NGN " + amount + "\n" +
                    "Account Number: " + account.getAccountNumber() + "\n" +
                    "Date: " + date + "\n" +
                    "Your available balance is " + account.getBalance() + "\n\n" +
                    "Thank you for banking with us!";

            EmailDetails emailDetails = EmailDetails.builder()
                    .recipient(customer.getEmail())
                    .subject("Debit Alert - " + account.getAccountNumber())
                    .messageBody(message)
                    .build();

            emailService.sendEmail(emailDetails);

        } catch (Exception e){
            log.error("Debit alert failed for account {}: {}", account.getAccountNumber(), e.getMessage());
        }
    }
    private void sendCreditAlert(Account account, BigDecimal amount, LocalDateTime date){
        try{
            Customer customer = getCustomer(account.getOwnerId());
            String message = "Dear " + customer.getFirstName() + ",\n\n" +
                    "Your account has been credited with NGN " + amount + "\n" +
                    "Date: " + date + "\n" +
                    "Your available balance is " + account.getBalance() + "\n\n" +
                    "Thank you for banking with us!";

            EmailDetails emailDetails = EmailDetails.builder()
                    .recipient(customer.getEmail())
                    .subject("Credit Alert - " + account.getAccountNumber())
                    .messageBody(message)
                    .build();
            emailService.sendEmail(emailDetails);

        } catch(Exception e){
            log.error("Credit alert failed for account {}: {}", account.getAccountNumber(), e.getMessage());
        }
    }

    private Customer getCustomer(UUID ownerId) {
        return customerRepository.findById(ownerId).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    private TransactionResponse buildTransactionResponse(TransactionStatus transactionStatus){
        return new TransactionResponse(transactionStatus);
    }
}
