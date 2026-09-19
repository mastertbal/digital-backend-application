package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.request.*;
import com.groupa.digitalbackendapplication.domain.dto.response.*;
import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.AuditLog;
import com.groupa.digitalbackendapplication.domain.entities.Customer;
import com.groupa.digitalbackendapplication.domain.enums.AccountStatus;
import com.groupa.digitalbackendapplication.domain.enums.AccountTier;
import com.groupa.digitalbackendapplication.domain.enums.Gender;
import com.groupa.digitalbackendapplication.domain.enums.Role;
import com.groupa.digitalbackendapplication.domain.entities.User;
import com.groupa.digitalbackendapplication.domain.enums.*;
import com.groupa.digitalbackendapplication.domain.dto.response.LogoutResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.Response;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.exceptions.ResourceNotFoundException;
import com.groupa.digitalbackendapplication.notification.EmailDetails;
import com.groupa.digitalbackendapplication.notification.EmailService;
import com.groupa.digitalbackendapplication.repository.AccountRepository;
import com.groupa.digitalbackendapplication.repository.AuditLogRepository;
import com.groupa.digitalbackendapplication.repository.CustomerRepository;
import com.groupa.digitalbackendapplication.security.AuthUser;
import com.groupa.digitalbackendapplication.service.*;
import com.groupa.digitalbackendapplication.utils.AccountUtil;
import com.groupa.digitalbackendapplication.utils.LoginSessionUtil;
import com.groupa.digitalbackendapplication.utils.EncryptionUtil;
import com.groupa.digitalbackendapplication.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor

public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final AccountUtil accountUtil;
    private final PasswordEncoder passwordEncoder;
    private final LoginSessionUtil loginSessionUtil;
    private final SecurityUtil securityUtil;
    private final EncryptionUtil encryptionUtil;
    private final AuditLogRepository auditLogRepository;
    private final OtpService otpService;
    private final LoginSessionService loginSessionService;
    private final RefreshSessionService refreshSessionService;
    private final EmailService emailService;

    @Override
    public ResponseWrapper<AccountCreatedResponse> createPersonalAccount(CustomerRegistrationRequest payload) {
        Role userRole = Role.CUSTOMER;
        AccountStatus accountStatus = AccountStatus.PENDING_VERIFICATION;
        AccountTier accountTier = AccountTier.TIER_1;
        PersonalAccountType accountType = PersonalAccountType.SAVINGS;

        if(validatePhoneNumber(payload.getPhoneNumber())) throw new BadRequestException("Error occurred: please provide another phone number");

        Optional<Customer> customerOptional = customerRepository.findByEmail(payload.getEmail());
        if(customerOptional.isPresent())
            throw new BadRequestException("Error occurred: please provide another email");

        SavedCustomerResponse userResponse = buildCustomerDetails(
                payload.getFirstName(), payload.getLastName(), payload.getEmail(), payload.getPassword(),
                payload.getPhoneNumber(), userRole, payload.getGender(), payload.getDateOfBirth(), payload.getAddress());

        String accountNumber = accountUtil.generateAccountNumber();

        //Continue account creation
        Account account = buildAccount(userResponse.getCustomer(), accountStatus, accountNumber, accountTier, accountType);
        AccountCreatedResponse createAccount = new AccountCreatedResponse(account.getAccountNumber());
        otpService.generateAndSendOtp(userResponse.getCustomer().getId(), account);

        // save audit log entry
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.USER_REGISTRATION)
                        .userId(userResponse.getCustomer().getId())
                        .userEmail(payload.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("customer")
                .build());

        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.ACCOUNT_CREATED)
                        .userId(userResponse.getCustomer().getId())
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
    public ResponseWrapper<AccountCreatedResponse> createOtherAccount(SecondaryAccountCreationRequest payload) {
        AuthUser loggedInUser = securityUtil.getSecurityPrincipal();
        loginSessionUtil.verify(loggedInUser.getUser().getId());

        Customer customer = customerRepository.findById(loggedInUser.getUser().getId())
                .orElseThrow(()-> new RuntimeException("Error occurred, try again"));

        Account savingsAccount = accountRepository.findByCustomerAndPersonalAccountType(customer, PersonalAccountType.SAVINGS)
                .orElseThrow(()-> new RuntimeException("Error occurred, try again"));

        if(savingsAccount.getAccountTier() != AccountTier.TIER_3)
            throw new BadRequestException("Account tier must be tier 3 to create other accounts");

        boolean exist = customer.getAccounts().stream().anyMatch(account -> account.getPersonalAccountType().equals(payload.type()));

        if(exist) throw
                new BadRequestException("Error occurred: cannot create more than one " + payload.type().toString());

        String accountNumber = accountUtil.generateAccountNumber();

        buildAccount(customer, AccountStatus.ACTIVE,accountNumber, savingsAccount.getAccountTier(), payload.type());
        AccountCreatedResponse createAccount = new AccountCreatedResponse(accountNumber);

        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.SECONDARY_ACCOUNT_CREATED)
                        .userId(customer.getId())
                        .userEmail(customer.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("accounts")
                        .build());

        sendAccountCreationEmail(customer.getFirstName(), customer.getEmail(),accountNumber,
                savingsAccount.getAccountTier(), payload.type());


        return ResponseWrapper.<AccountCreatedResponse>builder()
                .data(createAccount)
                .message("Account created successful")
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @Override
    public ResponseWrapper<String> setTransactionPin(ChangeTransactionPinRequest payload) {
        AuthUser loggedInUser = securityUtil.getSecurityPrincipal();
        loginSessionUtil.verify(loggedInUser.getUser().getId());

        Customer customer = customerRepository.findById(loggedInUser.getUser().getId())
                .orElseThrow(()-> new RuntimeException("Error occurred, try again"));

        if(customer.getTransactionCode() != null)
            throw new BadRequestException("Transaction code already set, " +
                    "use the forget pin if you have misplaced or forgot your pin");

        if(!payload.pin().equals(payload.confirmPin()))
            throw new BadRequestException("confirm pin doesn't match");

        String response = setTransactionPin(payload.confirmPin(), customer);

        return ResponseWrapper.<String>builder()
                .data(response)
                .message("success")
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @Override
    public ResponseWrapper<String> verifyTransactionPin(TransactionPinRequest payload) {
        AuthUser loggedInUser = securityUtil.getSecurityPrincipal();
        loginSessionUtil.verify(loggedInUser.getUser().getId());

        Customer customer = customerRepository.findById(loggedInUser.getUser().getId())
                .orElseThrow(()-> new RuntimeException("Error occurred, try again"));

        if(!passwordEncoder.matches(String.valueOf(payload.pin()), customer.getTransactionCode()))
            throw new BadCredentialsException("Pin doesn't match");

        return ResponseWrapper.<String>builder()
                .data("Pin verified")
                .message("success")
                .statusCode(HttpStatus.OK)
                .build();
    }

    @Override
    public ResponseWrapper<String> changeTransactionPin(ChangeTransactionPinRequest payload) {
        AuthUser loggedInUser = securityUtil.getSecurityPrincipal();
        loginSessionUtil.verify(loggedInUser.getUser().getId());

        Customer customer = customerRepository.findById(loggedInUser.getUser().getId())
                .orElseThrow(()-> new RuntimeException("Error occurred, try again"));

        if(!payload.pin().equals(payload.confirmPin()))
            throw new BadRequestException("confirm pin doesn't match");

        String response = setTransactionPin(payload.confirmPin(), customer);

        return ResponseWrapper.<String>builder()
                .data(response)
                .message("success")
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
    public ResponseWrapper<String> getUserNameByAccountNumber(String accountNumber) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));

        Customer customer = account.getCustomer();

        String accountName =  customer.getLastName() + " " + customer.getFirstName();

        return ResponseWrapper.<String>builder()
                .data(accountName)
                .statusCode(HttpStatus.OK)
                .message("success")
                .build();
    }

    @Override
    public Response<CustomerDto> getUserProfileById(UUID userId) {

        Customer customer = customerRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        Set<Account> accounts = customer.getAccounts();

        List<AccountDto> accountDTOs = customer.getAccounts().stream()
                .map(this::buildAccountDto)
                .toList();

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
                .accountDto(accountDTOs)
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

        logout();

        return ResponseWrapper.<String>builder()
                .message("Password reset successful, please login with the new password")
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
                .transactionCode(null)
                .address(address)
                .bvn(null)
                .nin(null)
                .build();
        Customer savedCustomer = customerRepository.save(customer);
        return new SavedCustomerResponse(savedCustomer);
    }

    private Account buildAccount(Customer customer, AccountStatus accountStatus, String accountNumber, AccountTier accountTier, PersonalAccountType accountType){
        Account account = Account.builder()
                .customer(customer)
                .accountStatus(accountStatus)
                .accountNumber(accountNumber)
                .accountTier(accountTier)
                .personalAccountType(accountType)
                .balance(BigDecimal.ZERO)
                .build();
        return accountRepository.save(account);
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

    private void logout(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        UUID userId = authUser.getUser().getId();

        loginSessionService.invalidateLoginSession(userId);

        refreshSessionService.invalidateLoginSession(userId);

        LogoutResponse logoutResponse = new LogoutResponse("Logout Successful");

        // save audit log
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.USER_LOGOUT)
                        .userId(userId)
                        .userEmail(authUser.getUser().getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("user")
                        .build());
    }

    private AccountDto buildAccountDto(Account account){

        AccountDto accountDto = AccountDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .accountTier(account.getAccountTier())
                .accountStatus(account.getAccountStatus())
                .accountType(account.getPersonalAccountType())
                .build();

        return accountDto;
    }

    private void sendAccountCreationEmail(String firstname, String email, String accountNumber, AccountTier accountTier, PersonalAccountType accountType) {
        String welcomeMessage = "Dear, " + firstname + "!\n\n" +
                "Your " + accountType.toString() +" account has been successfully Created and activated!\n\n" +
                "Below are your account details:\n\n" +
                "Account Number: " + accountNumber + "\n" +
                "Account Type: " + accountTier + "\n" +
                "Currency: NGN\n\n" +
                "Start enjoying seamless deposits, withdrawals, transfers and monthly statements. \n\n" +
                "Your financial journey starts here!";
        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(email)
                .subject("Secondary Account Creation")
                .messageBody(welcomeMessage)
                .build();
        emailService.sendEmail(emailDetails);
    }

    private String setTransactionPin(Integer pin, Customer customer){

        boolean isFourDigits = pin >= 1000 && pin <= 9999;

        if(!isFourDigits)
            throw new BadRequestException("Invalid pin: pin must be four digits");

        customer.setTransactionCode(passwordEncoder.encode(String.valueOf(pin)));
        customerRepository.save(customer);

        return "Transaction code set successful";
    }
}
