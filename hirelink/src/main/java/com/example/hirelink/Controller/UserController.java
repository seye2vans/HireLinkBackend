package com.example.hirelink.Controller;

import com.example.hirelink.Repositories.UserRepository;
import com.example.hirelink.User.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private UserRepository userRepository;
    @GetMapping
    public List<User> getAllUsers(){
        return userRepository.findAll();

    }
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}
