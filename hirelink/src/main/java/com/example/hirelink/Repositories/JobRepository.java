package com.example.hirelink.Repositories;

import com.example.hirelink.Job.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {

}
