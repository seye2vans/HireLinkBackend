package com.example.hirelink.Job;

import com.example.hirelink.User.User;
import com.example.hirelink.Job.Job;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter  // ✅ Lombok generates getters and setters
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Job job;

    private String coverLetter;

    private String resumePath;

    private String status = "Under Review";

    private LocalDateTime appliedDate = LocalDateTime.now();
}
