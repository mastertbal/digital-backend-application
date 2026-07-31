package com.groupa.digitalbackendapplication.notification;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j

public class EmailServiceImpl implements EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.name}")
    private String senderName;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    private final RestClient restClient = RestClient.create();
    @Override
    public void sendEmail(EmailDetails emailDetails) {
        Map<String, Object> sender = new HashMap<>();
        sender.put("name", senderName);
        sender.put("email", senderEmail);

        Map<String, Object> recipient = new HashMap<>();
        recipient.put("email", emailDetails.getRecipient());

        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put("sender", sender);
        messageBody.put("to", new Object[]{recipient});
        messageBody.put("subject", emailDetails.getSubject());
        messageBody.put("textContent", emailDetails.getMessageBody());

        try{
            restClient.post()
                    .uri("https://api.brevo.com/v3/smtp/email")
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .body(messageBody)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Email Sent Successfully to {}", emailDetails.getRecipient());
        } catch (Exception e){
            log.error("Failed to send email to {}: {}", emailDetails.getRecipient(), e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }

    }


}
