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
            // 1️⃣ Validate token header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
            }

            // 2️⃣ Extract and verify JWT token
            String token = authHeader.substring(7);
            String tokenEmail = jwtUtil.extractUsername(token);

            // 3️⃣ Check if token user exists
            User authUser = userRepository.findByEmail(tokenEmail)
                    .orElseThrow(() -> new RuntimeException("Invalid token user"));

            // 4️⃣ Allow only the user themselves or admins/employers to access
            if (!authUser.getEmail().equals(email) && authUser.getRole().name().equalsIgnoreCase("SEEKER")) {
                return ResponseEntity.status(403).body(Map.of("message", "Forbidden"));
            }

            // 5️⃣ Fetch requested user
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 6️⃣ Return full user info expected by frontend
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "phone", user.getPhone(),
                    "bio", user.getBio(),
                    "resume", user.getResume(),
                    "resumeFileName", user.getResumeFileName(),
                    "role", user.getRole().name()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/updateProfile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader,
                                           @RequestBody Map<String, Object> updates) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
            }

            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Update only allowed fields
            if (updates.containsKey("name")) user.setName((String) updates.get("name"));
            if (updates.containsKey("phone")) user.setPhone((String) updates.get("phone"));
            if (updates.containsKey("bio")) user.setBio((String) updates.get("bio"));
            if (updates.containsKey("resume")) user.setResume((String) updates.get("resume"));
            if (updates.containsKey("resumeFileName")) user.setResumeFileName((String) updates.get("resumeFileName"));

            userRepository.save(user);

            return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }




}
