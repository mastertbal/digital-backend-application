package com.groupa.digitalbackendapplication.domain.dto.response;

import com.groupa.digitalbackendapplication.domain.enums.Gender;
import com.groupa.digitalbackendapplication.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
public class AdminDto {

    private String adminId;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private Gender gender;

    private LocalDate dateOfBirth;

    private Role role;

    private String address;
}
