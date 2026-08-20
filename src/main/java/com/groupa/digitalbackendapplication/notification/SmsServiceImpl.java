package com.groupa.digitalbackendapplication.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    private final RestClient restClient;

    @Value("${termii.api.key}")
    private String apiKey;

    @Value("${termii.sender.id}")
    private String senderId;

    @Value("${termii.base.url}")
    private String baseUrl;

    public SmsServiceImpl() {
        this.restClient = RestClient.create();
    }

    @Override
    public void sendOtp(String phoneNumber, String otp) {
        Map<String, Object> requestBody = new HashMap<>();

        requestBody.put("to", phoneNumber);
        requestBody.put("from", senderId);

        requestBody.put("sms", "Your PAYEDGE Digital Banking OTP is " + otp + ",. It expires in 5 minutes. Do not share this code.");

        requestBody.put("type", "plaintext");
        requestBody.put("channel", "generic");
        requestBody.put("api_key", apiKey);

        try{
            restClient.post()
                    .uri(baseUrl + "/api/sms/send")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();

            log.info("OTP SMS sent successfully to {}", maskPhoneNumber(phoneNumber));
        } catch (Exception e){
            log.error("Failed to send OTP SMS to {}: {}", maskPhoneNumber(phoneNumber), e.getMessage());
            throw new RuntimeException("Failed to send OTP SMS", e);
        }
    }
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return "****"  + phoneNumber.substring(phoneNumber.length() - 4);
    }
}
