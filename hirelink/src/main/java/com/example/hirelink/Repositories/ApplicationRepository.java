package com.example.hirelink.Repositories;

import com.example.hirelink.Application.Application;
import com.example.hirelink.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // ✅ Use applicant instead of user
    List<Application> findByJob_IdAndApplicant_Email(Long jobId, String applicantEmail);

    void deleteByJob_Id(Long jobId);

    List<Application> findByJob_Employer(User employer);
}
