package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.request.ChangePasswordRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.CustomerRegistrationRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.*;
import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.AuditLog;
import com.groupa.digitalbackendapplication.domain.entities.Customer;
import com.groupa.digitalbackendapplication.domain.entities.User;
import com.groupa.digitalbackendapplication.domain.enums.*;
import com.groupa.digitalbackendapplication.domain.response.Response;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.exceptions.ResourceNotFoundException;
import com.groupa.digitalbackendapplication.notification.EmailDetails;
import com.groupa.digitalbackendapplication.notification.EmailService;
import com.groupa.digitalbackendapplication.repository.AccountRepository;
import com.groupa.digitalbackendapplication.repository.AuditLogRepository;
import com.groupa.digitalbackendapplication.repository.CustomerRepository;
import com.groupa.digitalbackendapplication.security.AuthUser;
import com.groupa.digitalbackendapplication.service.CustomerService;
import com.groupa.digitalbackendapplication.service.LoginSessionService;
import com.groupa.digitalbackendapplication.service.OtpService;
import com.groupa.digitalbackendapplication.utils.AccountUtil;
import com.groupa.digitalbackendapplication.utils.LoginSessionUtil;
import com.groupa.digitalbackendapplication.utils.EncryptionUtil;
import com.groupa.digitalbackendapplication.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor

public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final AccountUtil accountUtil;
    private final PasswordEncoder passwordEncoder;
    private final LoginSessionService loginSessionService;
    private final LoginSessionUtil loginSessionUtil;
    private final SecurityUtil securityUtil;
    private final EncryptionUtil encryptionUtil;
    private final EmailService emailService;
    private final AuditLogRepository auditLogRepository;
    private final OtpService otpService;

    @Override
    public ResponseWrapper<AccountCreatedResponse> createPersonalAccount(CustomerRegistrationRequest payload) {
        Role userRole = Role.CUSTOMER;
        AccountStatus accountStatus = AccountStatus.PENDING_VERIFICATION;
        AccountTier accountTier = AccountTier.TIER_1;

        if(validatePhoneNumber(payload.getPhoneNumber())) throw new BadRequestException("Error occurred: please provide another phone number");

        Optional<Customer> customerOptional = customerRepository.findByEmail(payload.getEmail());
        if(customerOptional.isPresent())
            throw new BadRequestException("Error occurred: please provide another email");

        SavedCustomerResponse userResponse = buildCustomerDetails(
                payload.getFirstName(), payload.getLastName(), payload.getEmail(), payload.getPassword(),
                payload.getPhoneNumber(), userRole, payload.getGender(), payload.getDateOfBirth(), payload.getAddress());

        String accountNumber = accountUtil.generateAccountNumber();

        //Continue account creation

        Account account = buildAccount(userResponse.getCustomerId(), accountStatus, accountNumber, accountTier);
        AccountCreatedResponse createAccount = new AccountCreatedResponse(account.getAccountNumber());
        otpService.generateAndSendOtp(userResponse.getCustomerId(), account);

        // save audit log entry
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.USER_REGISTRATION)
                        .userId(userResponse.getCustomerId())
                        .userEmail(payload.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("customer")
                .build());

        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.ACCOUNT_CREATED)
                        .userId(userResponse.getCustomerId())
                        .userEmail(payload.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("accounts")
                        .build());

        return ResponseWrapper.<AccountCreatedResponse>builder()
                .data(createAccount)
                .message("Account created successful." +
                        "Please verify your account with the OTP sent to you.")
                .statusCode(HttpStatus.CREATED)
                .build();
    }


    @Override
    public Response<CustomerDto> getUserProfile() {
        AuthUser loggedInUser = securityUtil.getSecurityPrincipal();
        loginSessionUtil.verify(loggedInUser.getUser().getId());
        return getUserProfileById(loggedInUser.getUser().getId());
    }

    @Override
    public Response<CustomerDto> getUserProfileById(UUID userId) {

        Customer customer = customerRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        Account account = accountRepository.findByOwnerId(customer.getId())
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));


        AccountDto accountDto = AccountDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .accountTier(account.getAccountTier())
                .accountStatus(account.getAccountStatus())
                .build();

        CustomerDto customerDto = CustomerDto.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .gender(customer.getGender())
                .dateOfBirth(customer.getDateOfBirth())
                .role(customer.getRole())
                .address(customer.getAddress())
                .nin(encryptionUtil.decrypt(customer.getNin()))
                .bvn(encryptionUtil.decrypt(customer.getBvn()))
                .accountDto(accountDto)
                .build();

        // save audit log
        User user = securityUtil.getSecurityPrincipal().getUser();
        if (user.getRole().equals(Role.CUSTOMER)) {
            auditLogRepository.save(
                    AuditLog.builder()
                            .actionType(ActionType.USER_PROFILE_FETCHED)
                            .userId(customer.getId())
                            .userEmail(customer.getEmail())
                            .timeOfCreation(LocalDateTime.now())
                            .entityType("users")
                            .build());
        } else {
            auditLogRepository.save(
                    AuditLog.builder()
                            .actionType(ActionType.USER_PROFILE_FETCHED)
                            .userId(user.getId())
                            .userEmail(user.getEmail())
                            .timeOfCreation(LocalDateTime.now())
                            .entityType("users")
                            .build());
        }

        return Response.<CustomerDto>builder()
                .data(customerDto)
                .message("Success")
                .statusCode(HttpStatus.OK.value())
                .build();
    }

    @Override
    public ResponseWrapper<String> changePassword(ChangePasswordRequest payload) {
        AuthUser loggedInUser = securityUtil.getSecurityPrincipal();

        Customer customer  = customerRepository.findById(loggedInUser.getUser().getId())
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        if(!payload.newPassword().equals(payload.confirmPassword()))
            throw new BadRequestException("Confirm password must be same as new password");

        customer.setPassword(passwordEncoder.encode(payload.confirmPassword()));
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);

        // save audit log
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.PASSWORD_CHANGED)
                        .userId(customer.getId())
                        .userEmail(customer.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("customer")
                        .build());

        return ResponseWrapper.<String>builder()
                .message("Password reset successful")
                .statusCode(HttpStatus.ACCEPTED)
                .build();
    }

    private SavedCustomerResponse buildCustomerDetails(String firstName, String lastName, String email, String password, String phoneNumber, Role role, Gender gender, LocalDate dateOfBirth, String address){

        Customer customer =Customer.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .phoneNumber(phoneNumber)
                .role(role)
                .gender(gender)
                .dateOfBirth(dateOfBirth)
                .address(address)
                .bvn(null)
                .nin(null)
                .build();
        Customer savedCustomer = customerRepository.save(customer);
        return new SavedCustomerResponse(savedCustomer.getId());
    }

    private Account buildAccount(UUID ownerId, AccountStatus accountStatus, String accountNumber, AccountTier accountTier){
        Account account = Account.builder()
                .ownerId(ownerId)
                .accountStatus(accountStatus)
                .accountNumber(accountNumber)
                .accountTier(accountTier)
                .balance(BigDecimal.ZERO)
                .build();
        return accountRepository.save(account);
    }

    private void sendAccountCreationEmail(String firstname, String email, String accountNumber, AccountTier accountTier){
        String message = "Hi, " + firstname + "!\n\n" +
                "Your account has been successfully created!\n\n" +
                "Below are your account details:\n\n" +
                "Account Number: " + accountNumber + "\n" +
                "Account Type: " + accountTier + "\n" +
                "Currency: NGN" + "\n\n" +
                "You can now login and start using your account immediately!";

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(email)
                .subject("Account Creation Successful")
                .messageBody(message)
                .build();
        emailService.sendEmail(emailDetails);
    }

    private ResponseWrapper<AuthResponse> buildAuthResponse(UUID id, String message, HttpStatusCode statusCode){

        AuthResponse authResponse =new AuthResponse(id);
        return ResponseWrapper.<AuthResponse>builder()
                .data(authResponse)
                .message(message)
                .statusCode(statusCode)
                .build();
    }

    private boolean validatePhoneNumber(String phoneNumber){
        Optional<Customer> customerOptional = customerRepository.findByPhoneNumber(phoneNumber);
        return customerOptional.isPresent();
    }

}
