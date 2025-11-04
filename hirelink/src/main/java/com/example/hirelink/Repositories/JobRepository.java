package com.example.hirelink.Repositories;

import com.example.hirelink.Job.Job;
import com.example.hirelink.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByEmployer(User employer);
    List<Job> findByEmployerId(Long employerId);


    @Query("SELECT j FROM Job j WHERE j.employer.email = :email")
    List<Job> findJobsByEmployerEmail(@Param("email") String email);
}
