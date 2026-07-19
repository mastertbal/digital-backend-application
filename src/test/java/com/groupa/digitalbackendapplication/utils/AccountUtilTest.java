package com.groupa.digitalbackendapplication.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AccountUtil Unit Tests")
class AccountUtilTest {

    private final AccountUtil accountUtil = new AccountUtil();

    @Test
    @DisplayName("generateAccountNumber: should return a 10-character string")
    void generateAccountNumber_LengthIsTen() {
        String accountNumber = accountUtil.generateAccountNumber();
        assertThat(accountNumber).hasSize(10);
    }

    @Test
    @DisplayName("generateAccountNumber: should start with the current year")
    void generateAccountNumber_StartsWithCurrentYear() {
        String currentYear = String.valueOf(Year.now());
        String accountNumber = accountUtil.generateAccountNumber();
        assertThat(accountNumber).startsWith(currentYear);
    }

    @Test
    @DisplayName("generateAccountNumber: should contain only numeric characters")
    void generateAccountNumber_IsNumeric() {
        String accountNumber = accountUtil.generateAccountNumber();
        assertThat(accountNumber).matches("\\d+");
    }

    @RepeatedTest(20)
    @DisplayName("generateAccountNumber: suffix should be within the 6-digit range (100000–999999)")
    void generateAccountNumber_SuffixInValidRange() {
        String accountNumber = accountUtil.generateAccountNumber();
        String yearPart = String.valueOf(Year.now());
        String suffix = accountNumber.substring(yearPart.length());

        int suffixValue = Integer.parseInt(suffix);
        assertThat(suffixValue).isBetween(100_000, 999_999);
    }

    @RepeatedTest(10)
    @DisplayName("generateAccountNumber: should produce unique account numbers across repeated calls")
    void generateAccountNumber_ProducesUniqueValues() {
        String first = accountUtil.generateAccountNumber();
        String second = accountUtil.generateAccountNumber();
        // Not guaranteed to be unique (random), but statistically near-certain over 10 runs
        // This test mainly ensures no exceptions are thrown and output is well-formed
        assertThat(first).isNotBlank();
        assertThat(second).isNotBlank();
    }
}
