package com.groupa.digitalbackendapplication.domain.dto.request;

import com.groupa.digitalbackendapplication.domain.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record AdminCreationRequest(
    @NotBlank(message = "First name cannot be null or empty")
    String firstName,

    @NotBlank(message = "Last name cannot be null or empty")
    String lastName,

    @NotBlank(message = "Email name cannot be null or empty")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password cannot be null or empty")
    @Pattern(regexp = "^(?=.*\\d).{11,16}$", message = "Password should contain at least one number, one letter and must be 11 to 16 characters")
    String password,

    @NotBlank(message = "Phone number cannot be null or empty")
    @Pattern(regexp = "0[7|8|9][0|1][0-9]{8}", message = "Invalid phone number format")
    String phoneNumber,

    @NotNull
    Gender gender,

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate dateOfBirth,

    @NotBlank(message = "Address cannot be null or empty")
    String address){}
