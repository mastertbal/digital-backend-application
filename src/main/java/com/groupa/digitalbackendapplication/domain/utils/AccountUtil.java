package com.groupa.digitalbackendapplication.domain.utils;

import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class AccountUtil {

    public String generateAccountNumber(){
        String year = String.valueOf(Year.now());

        int randNumber = ThreadLocalRandom.current()
                .nextInt(100000, 1000000); // 100000 to 999999

        return year + randNumber;
    }
}
