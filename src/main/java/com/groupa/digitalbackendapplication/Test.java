package com.groupa.digitalbackendapplication;

import java.time.YearMonth;

public class Test {
    public static void main(String[] args) {
        String regex = "^(?:RC|BN|IT|LP|LLP) \\d{5,7}$";
        String text = "LP 51234";
        System.out.println(text.matches(regex));
    }
}
