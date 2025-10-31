package com.example.hirelink.Controller;

import com.example.hirelink.LoginRequest;
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
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User dbUser = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!passwordEncoder.matches(request.getPassword(), dbUser.getPassword())) {
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
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
            }

            String token = authHeader.substring(7); // Remove "Bearer "
            String email = jwtUtil.extractUsername(token);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return ResponseEntity.ok(Map.of(
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "role", user.getRole().name()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid token"));
        }
    }
    // ✅ SECURE: Get user by email (requires Bearer token)
    @GetMapping("/user")
    public ResponseEntity<?> getUserByEmail(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String email) {

        try {
            // 1️⃣ Check Authorization header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
            }

            // 2️⃣ Extract and validate token
            String token = authHeader.substring(7);
            String tokenEmail = jwtUtil.extractUsername(token);

            // 3️⃣ Ensure the token corresponds to a valid user
            User authUser = userRepository.findByEmail(tokenEmail)
                    .orElseThrow(() -> new RuntimeException("Invalid token user"));

            // 4️⃣ Allow the request if:
            //     a) the logged-in user is requesting their own data, OR
            //     b) the logged-in user is an EMPLOYER or ADMIN
            if (!authUser.getEmail().equals(email) && authUser.getRole().name().equalsIgnoreCase("SEEKER")) {
                return ResponseEntity.status(403).body(Map.of("message", "Forbidden"));
            }

            // 5️⃣ Fetch requested user
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 6️⃣ Return user info safely
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "role", user.getRole().name()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }




}
