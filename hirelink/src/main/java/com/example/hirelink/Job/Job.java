package com.example.hirelink.Job;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.example.hirelink.Application.Application;
import com.example.hirelink.User.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String company;
    private String location;

    @Column(length = 2000)
    private String description;

    private String type;

    private String jobSalary;
    private String jobStatus;

    @ManyToOne
    @JoinColumn(name = "employer_id")
    private User employer;

    @CreationTimestamp
    private LocalDateTime postedDate;

    // Optional: dynamic count or can keep as field
    private int applicants;

    // Add applications list with cascade delete
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Application> applications;
}
