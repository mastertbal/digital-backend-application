package com.groupa.digitalbackendapplication.controller;

import tools.jackson.databind.ObjectMapper;
import com.groupa.digitalbackendapplication.domain.dto.request.CustomerRegistrationRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.AccountCreatedResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.enums.Gender;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.service.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@DisplayName("AccountController Integration Tests (MockMvc)")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    private CustomerRegistrationRequest buildValidRequest() {
        return new CustomerRegistrationRequest(
                "Jane",
                "Doe",
                "jane.doe@example.com",
                "securePass123",
                "08012345678",
                Gender.FEMALE,
                LocalDate.of(1995, 6, 15),
                "12 Lagos Street, Abuja",
                "12345678901",
                "98765432100"
        );
    }

    @Test
    @DisplayName("POST /api/create-personal-account: should return 200 OK on successful account creation")
    void createPersonalAccount_ValidRequest_Returns200() throws Exception {
        ResponseWrapper<AccountCreatedResponse> serviceResponse = ResponseWrapper.<AccountCreatedResponse>builder()
                .data(new AccountCreatedResponse("2026123456"))
                .message("Account Creation Successful")
                .statusCode(HttpStatus.CREATED)
                .build();

        when(customerService.createPersonalAccount(any())).thenReturn(serviceResponse);

        mockMvc.perform(post("/api/create-personal-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account Creation Successful"))
                .andExpect(jsonPath("$.data.accountNumber").value("2026123456"));
    }

    @Test
    @DisplayName("POST /api/create-personal-account: should return 400 when first name is blank")
    void createPersonalAccount_BlankFirstName_Returns400() throws Exception {
        CustomerRegistrationRequest request = buildValidRequest();
        request.setFirstName("");

        mockMvc.perform(post("/api/create-personal-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/create-personal-account: should return 400 when email format is invalid")
    void createPersonalAccount_InvalidEmail_Returns400() throws Exception {
        CustomerRegistrationRequest request = buildValidRequest();
        request.setEmail("not-a-valid-email");

        mockMvc.perform(post("/api/create-personal-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/create-personal-account: should return 400 when phone number format is invalid")
    void createPersonalAccount_InvalidPhoneNumber_Returns400() throws Exception {
        CustomerRegistrationRequest request = buildValidRequest();
        request.setPhoneNumber("12345");

        mockMvc.perform(post("/api/create-personal-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/create-personal-account: should return 400 when password is blank")
    void createPersonalAccount_BlankPassword_Returns400() throws Exception {
        CustomerRegistrationRequest request = buildValidRequest();
        request.setPassword("  ");

        mockMvc.perform(post("/api/create-personal-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/create-personal-account: should return 400 when service throws BadRequestException")
    void createPersonalAccount_ServiceThrowsBadRequest_Returns400() throws Exception {
        when(customerService.createPersonalAccount(any()))
                .thenThrow(new BadRequestException("User must be at least 18 years old"));

        mockMvc.perform(post("/api/create-personal-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User must be at least 18 years old"));
    }

    @Test
    @DisplayName("POST /api/create-personal-account: should return 400 when gender is null")
    void createPersonalAccount_NullGender_Returns400() throws Exception {
        CustomerRegistrationRequest request = buildValidRequest();
        request.setGender(null);

        mockMvc.perform(post("/api/create-personal-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
