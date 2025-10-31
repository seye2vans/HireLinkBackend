package com.example.hirelink.Repositories;

import com.example.hirelink.Job.Application;
import com.example.hirelink.Job.Job;
import com.example.hirelink.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // ✅ For checking if a user already applied for a specific job
    List<Application> findByJob_IdAndUser_Email(Long jobId, String userEmail);

    // ✅ For deleting all applications linked to a job
    void deleteByJobId(Long jobId);

    // ✅ For getting all applications submitted by a specific user (job seeker)
    List<Application> findByJob_Employer_Email(String email);

}
