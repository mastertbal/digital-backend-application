package com.groupa.digitalbackendapplication.notification;

import com.groupa.digitalbackendapplication.domain.enums.OtpChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j

public class OtpDeliveryServiceImpl implements OtpDeliveryService {
    private final EmailService emailService;

    @Override
    public OtpChannel sendOtp(String firstname, String phoneNumber, String email, String otp) {
        sendViaEmail(firstname, email, otp);
        log.info("OTP delivered successfully via email");
        return OtpChannel.EMAIL;
    }

    private void sendViaEmail(String firstname, String email, String otp) {
        String message = """
                Hello, %s
                
                Your PAYEDGE Digital Banking verification code is:
                
                %s
                
                This OTP will expire in 5 minutes.
                
                Please do not share this code with anyone.
                
                If you did not initiate this request, please contact PAYEDGE Digital support.
                
                Regards,
                PAYEDGE Digital Banking
                """.formatted(firstname, otp);

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(email)
                .subject("PAYEDGE Digital Banking - OTP Verification")
                .messageBody(message)
                .build();
        emailService.sendEmail(emailDetails);
    }


}
