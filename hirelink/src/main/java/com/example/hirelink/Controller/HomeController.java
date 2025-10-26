package com.example.hirelink.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class HomeController {
    @GetMapping("/")

    public String home() {
        return "Backend is running 🎉 — Spring Boot + PostgreSQL ready!";
    }
}
