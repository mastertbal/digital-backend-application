package com.groupa.digitalbackendapplication.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AccountCreatedResponse {

    private String accountNumber;
}
