package com.example.weather.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        if (args.length != 1 || args[0] == null || args[0].isBlank()) {
            System.err.println("Usage: PasswordHashGenerator <plain-text-password>");
            System.exit(1);
        }

        System.out.println(new BCryptPasswordEncoder(12).encode(args[0]));
    }
}
