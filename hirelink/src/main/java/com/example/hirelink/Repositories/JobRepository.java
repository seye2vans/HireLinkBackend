package com.example.hirelink.Repositories;

import com.example.hirelink.Job.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    // Use employer ID to avoid passing the full User object
    List<Job> findByEmployerId(Long employerId);

    // Optional: find jobs by employer email
    @Query("SELECT j FROM Job j WHERE j.employer.email = :email")
    List<Job> findJobsByEmployerEmail(@Param("email") String email);
}
