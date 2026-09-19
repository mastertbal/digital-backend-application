package com.groupa.digitalbackendapplication.notification;

import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.Customer;
import com.groupa.digitalbackendapplication.repository.CustomerRepository;
import com.groupa.digitalbackendapplication.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j

public class TransactionAlertServiceImpl implements TransactionAlertService {
    private final CustomerRepository customerRepository;
    private final EmailService emailService;

    @Override
    public void sendDebitAlert(Account account, BigDecimal amount, LocalDateTime now) {
        try{
            Customer customer = getCustomer(account.getCustomer().getId());
            String message = "Dear " + customer.getFirstName() + ",\n\n" +
                    "Your account has been debited with NGN " + amount + "\n" +
                    "Account Number: " + account.getAccountNumber() + "\n" +
                    "Date: " + now + "\n" +
                    "Available Balance: NGN " + account.getBalance() + "\n\n" +
                    "Thank you for banking with us.";

            EmailDetails emailDetails = EmailDetails.builder()
                    .recipient(customer.getEmail())
                    .subject("Debit Alert - " + account.getAccountNumber())
                    .messageBody(message)
                    .build();

            emailService.sendEmail(emailDetails);
            log.info("Debit alert sent successfully for account {}", account.getAccountNumber());

        } catch (Exception e) {
            log.error("Debit alert failed for account {}: {}", account.getAccountNumber(), e.getMessage());
        }
    }


    @Override
    public void sendCreditAlert(Account account, BigDecimal amount, LocalDateTime now) {
        try{
            Customer customer = getCustomer(account.getCustomer().getId());
            String message = "Dear " + customer.getFirstName() + ",\n\n" +
                    "Your account has been credited with NGN " + amount + "\n" +
                    "Account Number: " + account.getAccountNumber() + "\n" +
                    "Date: " + now + "\n" +
                    "Available Balance: NGN " + account.getBalance() + "\n\n" +
                    "Thank you for banking with us.";

            EmailDetails emailDetails = EmailDetails.builder()
                    .recipient(customer.getEmail())
                    .subject("Credit Alert - " + account.getAccountNumber())
                    .messageBody(message)
                    .build();
            emailService.sendEmail(emailDetails);
            log.info("Credit alert sent successfully for account {}", account.getAccountNumber());
        } catch (Exception e) {
            log.error("Credit alert failed for account {}: {}", account.getAccountNumber(), e.getMessage());
        }
    }

    @Override
    public void sendTransactionDeclinedAlert(Account account, BigDecimal amount, LocalDateTime now) {
        try{
            Customer customer = getCustomer(account.getCustomer().getId());
            String message = "Dear " + customer.getFirstName() + ",\n\n" +
                    "Your transaction of NGN " + amount + " was declined.\n" +
                    "Account Number: " + account.getAccountNumber() + "\n" +
                    "Date: " + now + "\n\n" +
                    "If you believe this is an error, please contact our support team.\n\n" +
                    "Thank you for banking with us.";
            EmailDetails emailDetails = EmailDetails.builder()
                    .recipient(customer.getEmail())
                    .subject("Transaction Declined")
                    .messageBody(message)
                    .build();
            emailService.sendEmail(emailDetails);
            log.error("Transaction Declined alert sent successfully for account {}", account.getAccountNumber());
        } catch (Exception e) {
            log.error("Transaction Declined alert failed for account {}: {}", account.getAccountNumber(), e.getMessage());
        }
    }

    private Customer getCustomer(UUID ownerId) {
        return customerRepository.findById(ownerId).orElseThrow(() -> new RuntimeException("Customer not found"));
    }

}
