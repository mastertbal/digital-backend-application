package com.groupa.digitalbackendapplication.notification;

import com.groupa.digitalbackendapplication.domain.enums.OtpChannel;

public interface OtpDeliveryService {

    OtpChannel sendOtp(
            String firstname,
            String phoneNumber,
            String email,
            String otp
    );
}
