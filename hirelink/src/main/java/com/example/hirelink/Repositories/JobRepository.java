package com.example.hirelink.Repositories;

import com.example.hirelink.Job.Job;
import com.example.hirelink.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    // ✅ Simple version using field relationship
    List<Job> findByEmployer(User employer);

    // ✅ OR: More efficient query-based version using employer email
    @Query("SELECT j FROM Job j WHERE j.employer.email = :email")
    List<Job> findJobsByEmployerEmail(@Param("email") String email);
}
