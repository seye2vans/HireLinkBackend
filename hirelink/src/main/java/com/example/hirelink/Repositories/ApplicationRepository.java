package com.example.hirelink.Repositories;

import com.example.hirelink.Job.Application;
import com.example.hirelink.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByJob_IdAndUser_Email(Long jobId, String userEmail);

    void deleteByJobId(Long jobId);

    // ✅ All applications for jobs owned by an employer
    List<Application> findByJob_Employer(User employer);
}
