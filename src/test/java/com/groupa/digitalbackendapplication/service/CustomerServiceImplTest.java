package com.groupa.digitalbackendapplication.service;

import com.groupa.digitalbackendapplication.domain.dto.request.CustomerRegistrationRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.AccountCreatedResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.Customer;
import com.groupa.digitalbackendapplication.domain.enums.Gender;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.repository.AccountRepository;
import com.groupa.digitalbackendapplication.repository.CustomerRepository;
import com.groupa.digitalbackendapplication.service.impl.CustomerServiceImpl;
import com.groupa.digitalbackendapplication.utils.AccountUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerServiceImpl Unit Tests")
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUtil accountUtil;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private CustomerRegistrationRequest validRequest;
    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        validRequest = new CustomerRegistrationRequest(
                "Jane",
                "Doe",
                "jane.doe@example.com",
                "securePassword123",
                "08012345678",
                Gender.FEMALE,
                LocalDate.of(1995, 6, 15),
                "12 Lagos Street, Abuja",
                "12345678901",
                "98765432100"
        );

        savedCustomer = Customer.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phoneNumber("08012345678")
                .build();
        savedCustomer.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should successfully create a personal account for a valid customer")
    void createPersonalAccount_Success() {
        when(customerRepository.findByPhoneNumber(validRequest.getPhoneNumber())).thenReturn(Optional.empty());
        when(customerRepository.findByEmail(validRequest.getEmail())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(accountUtil.generateAccountNumber()).thenReturn("2026123456");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseWrapper<AccountCreatedResponse> response = customerService.createPersonalAccount(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Account Creation Successful");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getAccountNumber()).isEqualTo("2026123456");
    }

    @Test
    @DisplayName("Should throw BadRequestException when customer is under 18 years old")
    void createPersonalAccount_UnderageCustomer_ThrowsBadRequestException() {
        validRequest.setDateOfBirth(LocalDate.now().minusYears(17));

        assertThatThrownBy(() -> customerService.createPersonalAccount(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("at least 18 years old");
    }

    @Test
    @DisplayName("Should throw BadRequestException when phone number is already registered")
    void createPersonalAccount_DuplicatePhoneNumber_ThrowsBadRequestException() {
        when(customerRepository.findByPhoneNumber(validRequest.getPhoneNumber()))
                .thenReturn(Optional.of(savedCustomer));

        assertThatThrownBy(() -> customerService.createPersonalAccount(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("phone number");
    }

    @Test
    @DisplayName("Should throw BadRequestException when email is already registered")
    void createPersonalAccount_DuplicateEmail_ThrowsBadRequestException() {
        when(customerRepository.findByPhoneNumber(validRequest.getPhoneNumber())).thenReturn(Optional.empty());
        when(customerRepository.findByEmail(validRequest.getEmail()))
                .thenReturn(Optional.of(savedCustomer));

        assertThatThrownBy(() -> customerService.createPersonalAccount(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("email");
    }

    @Test
    @DisplayName("Should accept customer who is exactly 18 years old today")
    void createPersonalAccount_ExactlyEighteen_Success() {
        validRequest.setDateOfBirth(LocalDate.now().minusYears(18));

        when(customerRepository.findByPhoneNumber(validRequest.getPhoneNumber())).thenReturn(Optional.empty());
        when(customerRepository.findByEmail(validRequest.getEmail())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(accountUtil.generateAccountNumber()).thenReturn("2026999999");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseWrapper<AccountCreatedResponse> response = customerService.createPersonalAccount(validRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
