package com.example.hirelink.Controller;

import com.example.hirelink.RegisterRequest;
import com.example.hirelink.Repositories.UserRepository;
import com.example.hirelink.Role.Role;
import com.example.hirelink.Security.JwtUtil;
import com.example.hirelink.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil; // ✅ inject JwtUtil

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        String roleStr = request.getRole().toUpperCase();

        // ✅ Block direct admin registration
        if (roleStr.equals("ADMIN")) {
            throw new RuntimeException("Admin registration not allowed");
        }

        // ✅ Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // ✅ Convert to enum safely
        try {
            user.setRole(Role.valueOf(roleStr));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role. Use APPLICANT or EMPLOYER only.");
        }

        userRepository.save(user);
        return Map.of("message", "User registered successfully!");
    }


    @PostMapping("/login")
    public Map<String, String> login(@RequestBody User user) {
        User dbUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // ✅ Handle null or admin roles safely
        if (dbUser.getRole() == null) {
            throw new RuntimeException("User role not assigned. Contact admin.");
        }

        if (dbUser.getRole().equals(Role.ADMIN)) {
            throw new RuntimeException("Admin login not allowed via public endpoint");
        }

        // ✅ Generate token
        String token = jwtUtil.generateToken(dbUser.getEmail());

        return Map.of(
                "token", token,
                "role", dbUser.getRole().name(),
                "message", "Login successful"
        );
    }
}
