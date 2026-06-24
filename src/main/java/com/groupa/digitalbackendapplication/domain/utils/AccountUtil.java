package com.groupa.digitalbackendapplication.domain.utils;

import java.time.Year;

public class AccountUtil {

    private String generateAccountNumber(){
        Year currentYear = Year.now();
        int min = 100000;
        int max = 999999;
        int randNumber = (int) Math.floor(Math.random() * (min - max + 1) + min);

        StringBuilder accountNumber = new StringBuilder();
        accountNumber.append(currentYear).append(randNumber);

        return accountNumber.toString();
    }
}
