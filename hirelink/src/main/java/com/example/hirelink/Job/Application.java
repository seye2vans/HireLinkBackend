package com.example.hirelink.Job;

import com.example.hirelink.User.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
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
