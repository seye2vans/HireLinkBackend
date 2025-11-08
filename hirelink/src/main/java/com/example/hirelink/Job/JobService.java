package com.example.hirelink.Job;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.hirelink.Repositories.JobRepository;

@Service
@Transactional
public class JobService {
    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public Job createJob(Job job) {
        return jobRepository.save(job);
    }

    public Job updateJob(Long id, Job updates) {
        Job existing = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        existing.setTitle(updates.getTitle());
        existing.setLocation(updates.getLocation());
        existing.setType(updates.getType());
        existing.setJobSalary(updates.getJobSalary());
        existing.setJobStatus(updates.getJobStatus());

        return jobRepository.save(existing);
    }

    public void incrementApplicants(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setApplicants(job.getApplicants() + 1);
        jobRepository.save(job);
    }
}
