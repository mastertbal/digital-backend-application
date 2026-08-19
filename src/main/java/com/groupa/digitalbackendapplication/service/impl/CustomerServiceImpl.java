package com.groupa.digitalbackendapplication.service.impl;

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

        if(validateAge(payload.getDateOfBirth())) throw new BadRequestException("User must be at least 18 years old");

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

        return getUserProfileById(loggedInUser.getUser().getId());
    }

    @Override
    public Response<CustomerDto> getUserProfileById(UUID userId) {

        Optional<User> userOptional =  userRepository.findById(userId);

        User user = userOptional.get();

        loginSessionUtil.verify(user.getId());

        Customer customer = customerRepository.findById(user.getId())
                .orElseThrow(() -> new BadRequestException("Cannot find user"));

        CustomerDto customerDto = CustomerDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .role(user.getRole())
                .address(customer.getAddress())
                .nin(encryptionUtil.decrypt(customer.getNin()))
                .bvn(encryptionUtil.decrypt(customer.getBvn()))
                .build();

        return Response.<CustomerDto>builder()
                .data(customerDto)
                .message("Success")
                .statusCode(HttpStatus.OK.value())
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
        Customer savedCustomer = userRepository.save(customer);
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
        Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
        return userOptional.isPresent();
    }

    private boolean validateAge(LocalDate dateOfBirth){
        return Period.between(dateOfBirth, LocalDate.now()).getYears() < 18;
    }
}
