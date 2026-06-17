package com.groupa.digitalbackendapplication.domain.request;

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
    @Pattern(regexp = "[RC|BN|IT|LP] [0-9]{6}", message = "Invalid cac format")
    private String cacNumber;

    @NotBlank(message = "Phone number cannot be null or empty")
    private String password;

    @NotBlank(message = "Business email cannot be null or empty")
    @Email
    private String businessEmail;
}
