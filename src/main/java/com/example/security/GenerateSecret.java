package com.example.security;

import java.security.SecureRandom;
import java.util.Base64;

public class GenerateSecret {

    public static void main(String[] args) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] keyBytes = new byte[32];
        secureRandom.nextBytes(keyBytes);

        String base64UrlEncodedKey = Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);

        System.out.println(base64UrlEncodedKey);
    }
}