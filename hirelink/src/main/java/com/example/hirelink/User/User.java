package com.example.hirelink.User;

import com.example.hirelink.Role.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Email
    @Column(unique = true)
    private String email;

    @JsonIgnore
    @NotBlank
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    // ✅ Optional profile fields
    private String phone;

    @Column(length = 2000)
    private String bio;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String resume; // Store Base64 resume or file URL

    private String resumeFileName;
}
