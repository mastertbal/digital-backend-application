package com.groupa.digitalbackendapplication.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BusinessRegistrationRequest {

    @NotBlank(message = "Business name cannot be null or empty")
    private String businessName;

    @NotBlank(message = "Business address cannot be null or empty")
    private String businessAddress;

    @NotBlank(message = "Cac number cannot be null or empty")
    @Pattern(regexp = "^(?:RC|BN|IT|LP|LLP) \\d{5,7}$", message = "Invalid cac format")
    private String cacNumber;

    @NotBlank(message = "Password cannot be null or empty")
    @Pattern(regexp = "^(?=.*\\d)(?=.*[A-Z]).{10,15}$", message = "Password should contain at least one number, one letter and must be 10 to 15 characters long")
    private String password;

    @NotBlank(message = "Business email cannot be null or empty")
    @Email
    private String businessEmail;
}
