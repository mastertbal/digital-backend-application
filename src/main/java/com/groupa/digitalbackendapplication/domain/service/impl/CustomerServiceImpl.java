package com.groupa.digitalbackendapplication.domain.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.request.CustomerRegistrationRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.*;
import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.Customer;
import com.groupa.digitalbackendapplication.domain.enums.AccountStatus;
import com.groupa.digitalbackendapplication.domain.enums.AccountTier;
import com.groupa.digitalbackendapplication.domain.enums.Gender;
import com.groupa.digitalbackendapplication.domain.enums.Role;
import com.groupa.digitalbackendapplication.domain.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.domain.repository.AccountRepository;
import com.groupa.digitalbackendapplication.domain.repository.CustomerRepository;
import com.groupa.digitalbackendapplication.domain.service.CustomerService;
import com.groupa.digitalbackendapplication.domain.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

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
                .message("Account Creation Successfull")
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    private SavedCustomerResponse buildCustomerDetails(String firstName, String lastName, String email, String password, String phoneNumber, Role role, Gender gender, LocalDate dateOfBirth, String address, String nin, String bvn){

        Customer customer =Customer.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(password)
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
