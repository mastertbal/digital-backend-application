package com.groupa.digitalbackendapplication.domain.enums;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum AccountTier {
    TIER_1(new BigDecimal("50000"), new BigDecimal("300000")),
    TIER_2(new BigDecimal("200000"), new BigDecimal("500000")),
    TIER_3(new BigDecimal("5000000"), new BigDecimal("999999999"));

    private final BigDecimal transferLimit;
    private final BigDecimal maxBalance;

    AccountTier(BigDecimal transferLimit, BigDecimal maxBalance) {
        this.transferLimit = transferLimit;
        this.maxBalance = maxBalance;
    }
}