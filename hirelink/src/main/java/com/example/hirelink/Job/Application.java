package com.example.hirelink.Job;

import com.example.hirelink.User.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
@Entity
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"password", "roles"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Job job;

    private String coverLetter;
    private String resumePath;
    private String status = "Under Review";
    private LocalDateTime appliedDate = LocalDateTime.now();
}
