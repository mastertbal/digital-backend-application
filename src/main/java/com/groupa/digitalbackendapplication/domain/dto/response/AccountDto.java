package com.groupa.digitalbackendapplication.domain.dto.response;

import com.groupa.digitalbackendapplication.domain.enums.AccountStatus;
import com.groupa.digitalbackendapplication.domain.enums.AccountTier;
import com.groupa.digitalbackendapplication.domain.enums.PersonalAccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {

    private UUID id;

    private String accountNumber;

    private BigDecimal balance;

    private AccountTier accountTier;

    private PersonalAccountType accountType;

    private AccountStatus accountStatus;


}
