package com.groupa.digitalbackendapplication.domain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankOverviewDto {

    private long totalAccount;
    private long totalActiveAccount;
    private long totalDormantAccount;
    private long totalSuspendedAccount;
    private long totalTier3Account;
    private long totalTier2Account;
    private long totalTier1Account;
}
