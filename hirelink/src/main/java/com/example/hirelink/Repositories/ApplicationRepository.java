package com.example.hirelink.Repositories;

import com.example.hirelink.Job.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByJob_IdAndUser_Email(Long jobId, String userEmail);
}
