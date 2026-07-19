package com.groupa.digitalbackendapplication.utils;

import com.groupa.digitalbackendapplication.domain.enums.RequeryTransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransactionRequeryUtil Unit Tests")
class TransactionRequeryUtilTest {

    private static final Set<String> VALID_STATUSES = Arrays.stream(RequeryTransactionStatus.values())
            .map(Enum::name)
            .collect(Collectors.toSet());

    @Test
    @DisplayName("requeryTransactionStatus: should return a non-null, non-blank status string")
    void requeryTransactionStatus_ReturnsNonBlankString() {
        String status = TransactionRequeryUtil.requeryTransactionStatus();
        assertThat(status).isNotBlank();
    }

    @RepeatedTest(50)
    @DisplayName("requeryTransactionStatus: should always return a valid RequeryTransactionStatus value")
    void requeryTransactionStatus_AlwaysReturnsValidStatus() {
        String status = TransactionRequeryUtil.requeryTransactionStatus();
        assertThat(VALID_STATUSES).contains(status);
    }

    @Test
    @DisplayName("requeryTransactionStatus: should only produce SUCCESSFUL or DECLINED over many iterations")
    void requeryTransactionStatus_OnlySuccessfulOrDeclined() {
        Set<String> observed = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            observed.add(TransactionRequeryUtil.requeryTransactionStatus());
        }
        assertThat(observed).isSubsetOf("SUCCESSFUL", "DECLINED");
    }

    @Test
    @DisplayName("requeryTransactionStatus: result is parseable as a TransactionStatus enum value")
    void requeryTransactionStatus_ParseableAsTransactionStatus() {
        String status = TransactionRequeryUtil.requeryTransactionStatus();
        // Ensures the returned string is safe to pass to TransactionStatus.valueOf(...)
        assertThat(status).isIn("SUCCESSFUL", "DECLINED");
    }
}
