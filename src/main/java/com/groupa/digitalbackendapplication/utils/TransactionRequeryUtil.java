package com.groupa.digitalbackendapplication.utils;

import com.groupa.digitalbackendapplication.domain.enums.RequeryTransactionStatus;

import java.util.Random;

public class TransactionRequeryUtil {
    private static final Random random = new Random();

    public static String requeryTransactionStatus(){
        RequeryTransactionStatus[] statuses = RequeryTransactionStatus.values();
        int randomNumber = random.nextInt(statuses.length);

        return statuses[randomNumber].toString();
    }
}
