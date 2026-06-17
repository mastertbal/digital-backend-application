package com.groupa.digitalbackendapplication.domain.request;

import com.groupa.digitalbackendapplication.domain.entities.AccountType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRegistrationRequest {

    @NotBlank(message = "First name cannot be null or empty")
    private String firstName;

    @NotBlank(message = "Last name cannot be null or empty")
    private String lastName;

    @NotBlank(message = "Email name cannot be null or empty")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be null or empty")
    private String password;

    @NotBlank(message = "Phone number cannot be null or empty")
    @Pattern(regexp = "0[7|8|9][0|1][0-9]{8}", message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "Address cannot be null or empty")
    private String address;
}
