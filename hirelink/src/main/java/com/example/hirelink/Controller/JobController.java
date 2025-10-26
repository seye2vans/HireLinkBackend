package com.example.hirelink.Controller;

import com.example.hirelink.Job.Job;
import com.example.hirelink.Repositories.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobRepository jobRepository;
    @GetMapping
    public List<Job> getAllJobs(){
        return jobRepository.findAll();


    }
    @PostMapping
    public Job createJob(@RequestBody Job job){
        return jobRepository.save(job);
    }
}

