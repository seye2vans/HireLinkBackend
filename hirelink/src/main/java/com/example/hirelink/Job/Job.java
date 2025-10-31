package com.example.hirelink.Job;

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

    private String salary;   // e.g., "$80,000/year"
    private String status;   // e.g., "Active" or "Closed"

    @ManyToOne
    @JoinColumn(name = "employer_id")
    private User employer;
}
