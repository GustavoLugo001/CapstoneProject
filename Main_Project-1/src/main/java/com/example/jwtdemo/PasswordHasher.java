package com.example.jwtdemo;
import java.util.Base64;
import java.security.SecureRandom;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
public class PasswordHasher {
	public static void main(String[] args) {
		 byte[] key = new byte[32]; // 32 bytes = 256 bits
	        new SecureRandom().nextBytes(key);
	        String base64Key = Base64.getEncoder().encodeToString(key);
	        System.out.println("Your new JWT Secret Key (Base64): " + base64Key);

    }

}
