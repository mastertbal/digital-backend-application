package com.groupa.digitalbackendapplication.utils;

import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.DailyTransferTotal;
import com.groupa.digitalbackendapplication.domain.enums.AccountTier;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.repository.DailyTransferTotalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class TierLimiterUtil {

    private final DailyTransferTotalRepository dailyTransferTotalRepository;

    public void validateNotAlreadyOverTierMaxBalance(Account sourceAccount) {
        AccountTier tier = sourceAccount.getAccountTier();
        if (sourceAccount.getBalance().compareTo(tier.getMaxBalance()) > 0) {
            throw new BadRequestException("Tier maximum balance exceeded, upgrade tier to be able to make transfer");
        }
    }

    public BigDecimal validateDailyTransferLimit(Account sourceAccount, BigDecimal amount) {
        LocalDate today = LocalDate.now();
        BigDecimal existingTotal = dailyTransferTotalRepository
                .findForUpdate(sourceAccount.getAccountNumber(), today)
                .map(DailyTransferTotal::getTotalAmount)
                .orElse(BigDecimal.ZERO);

        BigDecimal newTotal = existingTotal.add(amount);
        AccountTier tier = sourceAccount.getAccountTier();

        if (newTotal.compareTo(tier.getTransferLimit()) > 0) {
            throw new BadRequestException("Transaction limit exceeded");
        }
        return newTotal;
    }

    public void recordDailyTransferTotal(String accountNumber, BigDecimal newTotal) {
        LocalDate today = LocalDate.now();
        DailyTransferTotal record = dailyTransferTotalRepository
                .findForUpdate(accountNumber, today)
                .orElseGet(() -> new DailyTransferTotal(accountNumber, today, BigDecimal.ZERO));
        record.setTotalAmount(newTotal);
        dailyTransferTotalRepository.save(record);
    }
}
