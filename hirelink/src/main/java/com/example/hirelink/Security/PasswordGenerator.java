package com.example.hirelink.Security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordGenerator implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        String rawPassword = "Admin123!"; // change this to your desired admin password
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashed = encoder.encode(rawPassword);
        System.out.println("🔐 Hashed password for admin: " + hashed);
    }
}