package com.example.hirelink.Controller;

import com.example.hirelink.RegisterRequest;
import com.example.hirelink.Repositories.UserRepository;
import com.example.hirelink.Role.Role;
import com.example.hirelink.Security.JwtUtil;
import com.example.hirelink.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ✅ REGISTER ENDPOINT
    @PostMapping("/register")
    public Map<String, String> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        String roleStr = request.getRole().toUpperCase();

        if (roleStr.equals("ADMIN")) {
            throw new RuntimeException("Admin registration not allowed");
        }

        Role role;
        try {
            role = Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role. Use SEEKER or EMPLOYER only.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        userRepository.save(user);

        return Map.of("message", "User registered successfully!");
    }


    // ✅ LOGIN ENDPOINT
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            User dbUser = userRepository.findByEmail(user.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!passwordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
                return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
            }

            String token = jwtUtil.generateToken(dbUser.getEmail());

            return ResponseEntity.ok(Map.of(
                    "email", dbUser.getEmail(),
                    "role", dbUser.getRole().name(),
                    "token", token
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

}
