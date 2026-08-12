package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.request.CustomerRegistrationRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.*;
import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.Customer;
import com.groupa.digitalbackendapplication.domain.entities.LoginSession;
import com.groupa.digitalbackendapplication.domain.enums.AccountStatus;
import com.groupa.digitalbackendapplication.domain.enums.AccountTier;
import com.groupa.digitalbackendapplication.domain.enums.Gender;
import com.groupa.digitalbackendapplication.domain.enums.Role;
import com.groupa.digitalbackendapplication.domain.response.Response;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.repository.AccountRepository;
import com.groupa.digitalbackendapplication.repository.CustomerRepository;
import com.groupa.digitalbackendapplication.security.AuthUser;
import com.groupa.digitalbackendapplication.service.CustomerService;
import com.groupa.digitalbackendapplication.service.LoginSessionService;
import com.groupa.digitalbackendapplication.utils.AccountUtil;
import com.groupa.digitalbackendapplication.utils.LoginSessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final AccountUtil accountUtil;
    private final PasswordEncoder passwordEncoder;
    private final LoginSessionService loginSessionService;
    private final LoginSessionUtil loginSessionUtil;

    @Override
    public ResponseWrapper<AccountCreatedResponse> createPersonalAccount(CustomerRegistrationRequest payload) {
        Role userRole = Role.CUSTOMER;
        AccountStatus accountStatus = AccountStatus.ACTIVE;
        AccountTier accountTier = AccountTier.TIER_1;

        if(validateAge(payload.getDateOfBirth())) throw
        new BadRequestException("User must be at least 18 years old");

        if(validatePhoneNumber(payload.getPhoneNumber())) throw
                new BadRequestException("Error occurred: please provide another phone number");

        Optional<Customer> customerOptional = customerRepository.findByEmail(payload.getEmail());
        if(customerOptional.isPresent()) throw new BadRequestException("Error occurred: please provide another email");

        SavedCustomerResponse userResponse = buildCustomerDetails(
                payload.getFirstName(), payload.getLastName(), payload.getEmail(), payload.getPassword(),
                payload.getPhoneNumber(), userRole, payload.getGender(), payload.getDateOfBirth(), payload.getAddress(), payload.getNin(), payload.getBvn());

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
    public ResponseWrapper<AccountCreatedResponse> createAdminAccount(CustomerRegistrationRequest payload) {
        Role userRole = Role.ADMIN;
        AccountStatus accountStatus = AccountStatus.ACTIVE;
        AccountTier accountTier = AccountTier.TIER_1;

        if(validateAge(payload.getDateOfBirth())) throw
        new BadRequestException("User must be at least 18 years old");

        if(validatePhoneNumber(payload.getPhoneNumber())) throw
                new BadRequestException("Error occurred: please provide another phone number");

        Optional<Customer> customerOptional = customerRepository.findByEmail(payload.getEmail());
        if(customerOptional.isPresent()) throw new BadRequestException("Error occurred: please provide another email");

        SavedCustomerResponse userResponse = buildCustomerDetails(
                payload.getFirstName(), payload.getLastName(), payload.getEmail(), payload.getPassword(),
                payload.getPhoneNumber(), userRole, payload.getGender(), payload.getDateOfBirth(), payload.getAddress(), payload.getNin(), payload.getBvn());

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) throw new BadRequestException("User profile not available");

        AuthUser userDetails = (AuthUser) authentication.getPrincipal();
        Optional<Customer> customerOptional = customerRepository.findByEmail(userDetails.getUsername());

        Customer customer = customerOptional.get();

        loginSessionUtil.verify(customer.getId());

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
                .build();

        if (customerDto.getNin() != null) customerDto.setNin(customerDto.getNin());
        if (customerDto.getBvn() != null) customerDto.setBvn(customerDto.getBvn());

        return Response.<CustomerDto>builder()
                .data(customerDto)
                .message("Success")
                .statusCode(HttpStatus.OK.value())
                .build();
    }

    private SavedCustomerResponse buildCustomerDetails(String firstName, String lastName, String email, String password, String phoneNumber, Role role, Gender gender, LocalDate dateOfBirth, String address, String nin, String bvn){

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
                .bvn(bvn)
                .nin(nin)
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
