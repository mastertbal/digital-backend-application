package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.request.ResendOtpRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.VerifyOtpRequest;
import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.Customer;
import com.groupa.digitalbackendapplication.domain.entities.OtpVerification;
import com.groupa.digitalbackendapplication.domain.enums.AccountStatus;
import com.groupa.digitalbackendapplication.domain.enums.OtpChannel;
import com.groupa.digitalbackendapplication.domain.response.Response;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.exceptions.ResourceNotFoundException;
import com.groupa.digitalbackendapplication.notification.EmailDetails;
import com.groupa.digitalbackendapplication.notification.EmailService;
import com.groupa.digitalbackendapplication.notification.OtpDeliveryService;
import com.groupa.digitalbackendapplication.repository.AccountRepository;
import com.groupa.digitalbackendapplication.repository.CustomerRepository;
import com.groupa.digitalbackendapplication.repository.OtpVerificationRepository;
import com.groupa.digitalbackendapplication.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j

public class OtpServiceImpl implements OtpService {
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OtpDeliveryService otpDeliveryService;

    private final SecureRandom secureRandom = new SecureRandom();

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_OTP_ATTEMPTS = 5;

    @Override
    @Transactional
    public Response<String> generateAndSendOtp(UUID customerId, Account account) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (account.getAccountStatus() == AccountStatus.ACTIVE) {
            throw new BadRequestException("Account is already active.");
        }

        otpVerificationRepository.findLatestByCustomerId(customerId).ifPresent(previousOtp -> {
            previousOtp.setVerifiedAt(LocalDateTime.now());
            otpVerificationRepository.save(previousOtp);
        });

        String otp = generateOtp();
        String otpHash = passwordEncoder.encode(otp);
        OtpChannel channel = otpDeliveryService.sendOtp(
                customer.getFirstName(),
                customer.getPhoneNumber(),
                customer.getEmail(),
                otp
        );

        OtpVerification otpVerification = OtpVerification.builder()
                .customerId(customerId)
                .otpHash(otpHash)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .attemptCount(0)
                .channel(channel)
                .build();
        otpVerificationRepository.save(otpVerification);

        log.info("OTP generated successfully for customer {} via {}", customerId, channel);

        return Response.<String>builder()
                .statusCode(HttpStatus.OK.value())
                .message("OTP sent successfully!")
                .data("OTP sent via " + channel.name())
                .build();
    }

    private String generateOtp() {
        int otp = secureRandom.nextInt(900000) + 100000;

        return String.valueOf(otp);
    }

    @Override
    public Response<String> verifyOtp(VerifyOtpRequest request) {

        UUID customerId = request.getCustomerId();
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Account account = accountRepository.findByOwnerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (account.getAccountStatus() == AccountStatus.ACTIVE) {
            throw new BadRequestException("Account is already active.");
        }

        OtpVerification otpVerification = otpVerificationRepository.findLatestByCustomerId(customerId).orElseThrow(() -> new ResourceNotFoundException("No Otp found. Please request a new OTP"));

        if (otpVerification.getVerifiedAt() !=null){
            throw new BadRequestException("Otp has already been used.");
        }
        if (LocalDateTime.now().isAfter(otpVerification.getExpiresAt())) {
            throw new BadRequestException("Otp has expired. please request a new OTP.");
        }

        if (otpVerification.getAttemptCount() >= MAX_OTP_ATTEMPTS) {
            throw new BadRequestException("Maximum OTP attempts exceeded. Please request a new OTP.");
        }

        if (!passwordEncoder.matches(request.getOtp(), otpVerification.getOtpHash())) {
            otpVerification.setAttemptCount(otpVerification.getAttemptCount() + 1);
            otpVerificationRepository.save(otpVerification);
            throw new BadRequestException("Invalid OTP.");
        }

        otpVerification.setVerifiedAt(LocalDateTime.now());
        otpVerificationRepository.save(otpVerification);

        account.setAccountStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        try{
            sendWelcomeEmail(customer.getFirstName(), customer.getEmail());
        } catch (Exception e) {
            log.error("Welcome email failed to send {}: {}", customer.getEmail(), e.getMessage());
        }

        log.info("Customer {} successfully verified OTP", customerId);
        return Response.<String>builder()
                .statusCode(HttpStatus.OK.value())
                .message("OTP verification successful")
                .data("Account activated successfully!")
                .build();
    }

    @Override
    @Transactional
    public Response<String> resendOtp(ResendOtpRequest request) {
        UUID customerId = request.getCustomerId();
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Account account = accountRepository.findByOwnerId(customerId).orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (account.getAccountStatus() == AccountStatus.ACTIVE) {
            throw new BadRequestException("Account is already active.");
        }
        return generateAndSendOtp(customerId, account);
    }
    private void sendWelcomeEmail(String firstname, String email){
        String welcomeMessage = "Welcome, " + firstname + "!\n\n" +
                "We are excited to have you on board at PAYEDGE DIGITAL BANKING. \n\n" +
                "Start enjoying seamless deposits, withdrawals, transfers and monthly statements. \n\n" +
                "Your financial journey starts here!";
        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(email)
                .subject("Welcome to PayEdge Digital Banking")
                .messageBody(welcomeMessage)
                .build();
        emailService.sendEmail(emailDetails);
    }
}
