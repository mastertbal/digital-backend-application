package com.groupa.digitalbackendapplication.notification;

public interface SmsService {
    void sendOtp(String phoneNumber, String otp);
}
