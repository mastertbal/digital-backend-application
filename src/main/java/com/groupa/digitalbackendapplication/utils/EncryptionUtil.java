package com.groupa.digitalbackendapplication.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtil {

    private final TextEncryptor textEncryptor;

    public EncryptionUtil(@Value("${ENCRYPTION_PASSWORD}") String password, @Value("${ENCRYPTION_SALT}") String salt){
        this.textEncryptor = Encryptors.delux(password, salt);
    }

    public String encrypt(String plainText){
        if(plainText == null)
            return null;
        return textEncryptor.encrypt(plainText);
    }

    public String decrypt(String encryptedText){
        if(encryptedText == null)
            return null;
        return textEncryptor.decrypt(encryptedText);
    }
}
