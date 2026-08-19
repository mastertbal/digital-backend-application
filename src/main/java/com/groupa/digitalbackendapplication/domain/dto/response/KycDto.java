package com.groupa.digitalbackendapplication.domain.dto.response;

import com.groupa.digitalbackendapplication.domain.enums.AccountTier;
import com.groupa.digitalbackendapplication.domain.enums.KycDocumentType;
import com.groupa.digitalbackendapplication.domain.enums.KycStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class KycDto {

    private UUID accountId;
    private UUID kycId;
    private String customerName;
    private KycDocumentType documentType;
    private String submittedValue;
    private KycStatus status;
    private AccountTier resultingTier;
    private LocalDateTime submittedAt;
}
