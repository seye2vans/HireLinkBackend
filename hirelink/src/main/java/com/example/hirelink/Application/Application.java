package com.example.hirelink.Application;

import com.example.hirelink.Job.Job;
import com.example.hirelink.User.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    @ManyToOne
    @JoinColumn(name = "applicant_id")
    private User applicant;

    private String resumeUrl;
    private String coverLetter;
    private String status;
    private LocalDateTime appliedDate;
}
