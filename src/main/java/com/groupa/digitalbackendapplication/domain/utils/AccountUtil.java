package com.groupa.digitalbackendapplication.domain.utils;

import java.time.Year;
import java.util.concurrent.ThreadLocalRandom;

public class AccountUtil {

    private String generateAccountNumber(){
        String year = String.valueOf(Year.now());

        int randNumber = ThreadLocalRandom.current()
                .nextInt(100000, 1000000); // 100000 to 999999

        return year + randNumber;
    }
}
