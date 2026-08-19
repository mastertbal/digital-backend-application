package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.request.ChangePasswordRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.CustomerRegistrationRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.*;
import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.Customer;
import com.groupa.digitalbackendapplication.domain.entities.User;
import com.groupa.digitalbackendapplication.domain.enums.AccountStatus;
import com.groupa.digitalbackendapplication.domain.enums.AccountTier;
import com.groupa.digitalbackendapplication.domain.enums.Gender;
import com.groupa.digitalbackendapplication.domain.enums.Role;
import com.groupa.digitalbackendapplication.domain.response.Response;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.exceptions.ResourceNotFoundException;
import com.groupa.digitalbackendapplication.notification.EmailDetails;
import com.groupa.digitalbackendapplication.notification.EmailService;
import com.groupa.digitalbackendapplication.repository.AccountRepository;
import com.groupa.digitalbackendapplication.repository.CustomerRepository;
import com.groupa.digitalbackendapplication.repository.UserRepository;
import com.groupa.digitalbackendapplication.security.AuthUser;
import com.groupa.digitalbackendapplication.service.CustomerService;
import com.groupa.digitalbackendapplication.service.LoginSessionService;
import com.groupa.digitalbackendapplication.utils.AccountUtil;
import com.groupa.digitalbackendapplication.utils.LoginSessionUtil;
import com.groupa.digitalbackendapplication.utils.EncryptionUtil;
import com.groupa.digitalbackendapplication.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor

public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final AccountUtil accountUtil;
    private final PasswordEncoder passwordEncoder;
    private final LoginSessionService loginSessionService;
    private final LoginSessionUtil loginSessionUtil;
    private final SecurityUtil securityUtil;
    private final EncryptionUtil encryptionUtil;
    private final EmailService emailService;

    @Override
    public ResponseWrapper<AccountCreatedResponse> createPersonalAccount(CustomerRegistrationRequest payload) {
        Role userRole = Role.CUSTOMER;
        AccountStatus accountStatus = AccountStatus.ACTIVE;
        AccountTier accountTier = AccountTier.TIER_1;

        if(validatePhoneNumber(payload.getPhoneNumber())) throw new BadRequestException("Error occurred: please provide another phone number");

        Optional<User> customerOptional = userRepository.findByEmail(payload.getEmail());
        if(customerOptional.isPresent()) throw new BadRequestException("Error occurred: please provide another email");

        SavedCustomerResponse userResponse = buildCustomerDetails(
                payload.getFirstName(), payload.getLastName(), payload.getEmail(), payload.getPassword(),
                payload.getPhoneNumber(), userRole, payload.getGender(), payload.getDateOfBirth(), payload.getAddress());

        String accountNumber = accountUtil.generateAccountNumber();
        //Continue account creation
        AccountCreatedResponse createAccount = buildAccount(userResponse.getCustomerId(), accountStatus, accountNumber, accountTier);

        try {
            sendWelcomeEmail(payload.getFirstName(), payload.getEmail());
        } catch (Exception e){
            log.error("Customer welcome email failed to send to {}: {}", payload.getEmail(), e.getMessage());
        }
        try {
            sendAccountCreationEmail(payload.getFirstName(), payload.getEmail(), createAccount.getAccountNumber(), accountTier);
        } catch (Exception e){
            log.error("Welcome email could not be sent to {}: {}", payload.getEmail(), e.getMessage());
        }

        return ResponseWrapper.<AccountCreatedResponse>builder()
                .data(createAccount)
                .message("Account Creation Successful")
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @Override
    public ResponseWrapper<AccountCreatedResponse> createAdminAccount(CustomerRegistrationRequest payload) {
        Role userRole = Role.ADMIN;
        AccountStatus accountStatus = AccountStatus.ACTIVE;
        AccountTier accountTier = AccountTier.TIER_1;

        if(validateAge(payload.getDateOfBirth())) throw
        new BadRequestException("User must be at least 18 years old");

        if(validatePhoneNumber(payload.getPhoneNumber())) throw
                new BadRequestException("Error occurred: please provide another phone number");

        Optional<User> customerOptional = userRepository.findByEmail(payload.getEmail());
        if(customerOptional.isPresent()) throw new BadRequestException("Error occurred: please provide another email");

        SavedCustomerResponse userResponse = buildCustomerDetails(
                payload.getFirstName(), payload.getLastName(), payload.getEmail(), payload.getPassword(),
                payload.getPhoneNumber(), userRole, payload.getGender(), payload.getDateOfBirth(), payload.getAddress());

        String accountNumber = accountUtil.generateAccountNumber();
        //Continue account creation

        AccountCreatedResponse createAccount = buildAccount(userResponse.getCustomerId(), accountStatus, accountNumber, accountTier);

        return ResponseWrapper.<AccountCreatedResponse>builder()
                .data(createAccount)
                .message("Account Creation Successful")
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @Override
    public Response<CustomerDto> getUserProfile() {
        AuthUser loggedInUser = securityUtil.getSecurityPrincipal();
        loginSessionUtil.verify(loggedInUser.getCustomer().getId());
        return getUserProfileById(loggedInUser.getCustomer().getId());
    }

    @Override
    public Response<CustomerDto> getUserProfileById(UUID userId) {

        Optional<User> userOptional =  userRepository.findById(userId);
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        User user = userOptional.get();
        Account account = accountRepository.findByOwnerId(customer.getId())
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));

        loginSessionUtil.verify(user.getId());

        Customer customer = customerRepository.findById(user.getId())
                .orElseThrow(() -> new BadRequestException("Cannot find user"));
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

        return Response.<CustomerDto>builder()
                .data(customerDto)
                .message("Success")
                .statusCode(HttpStatus.OK.value())
                .build();
    }

    @Override
    public ResponseWrapper<String> changePassword(ChangePasswordRequest payload) {
        AuthUser loggedInUser = securityUtil.getSecurityPrincipal();

        Customer customer  = customerRepository.findById(loggedInUser.getCustomer().getId())
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        if(!payload.newPassword().equals(payload.confirmPassword()))
            throw new BadRequestException("Confirm password must be same as new password");

        customer.setPassword(passwordEncoder.encode(payload.confirmPassword()));
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);

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

    private AccountCreatedResponse buildAccount(UUID ownerId, AccountStatus accountStatus, String accountNumber, AccountTier accountTier){
        Account account = Account.builder()
                .ownerId(ownerId)
                .accountStatus(accountStatus)
                .accountNumber(accountNumber)
                .accountTier(accountTier)
                .balance(BigDecimal.ZERO)
                .build();
        accountRepository.save(account);
        return new AccountCreatedResponse(account.getAccountNumber());
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

    private boolean validateAge(LocalDate dateOfBirth){
        return Period.between(dateOfBirth, LocalDate.now()).getYears() < 18;
    }
}
