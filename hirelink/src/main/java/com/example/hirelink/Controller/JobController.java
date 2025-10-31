package com.example.hirelink.Controller;

import com.example.hirelink.Job.Job;
import com.example.hirelink.Repositories.JobRepository;
import com.example.hirelink.Repositories.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    @GetMapping
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    @PostMapping
    public Job createJob(@RequestBody Job job) {
        return jobRepository.save(job);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id) {
        return jobRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Update job
    @PutMapping("/{id}")
    public ResponseEntity<Job> updateJob(@PathVariable Long id, @RequestBody Job updatedJob) {
        return jobRepository.findById(id)
                .map(existingJob -> {
                    existingJob.setTitle(updatedJob.getTitle());
                    existingJob.setCompany(updatedJob.getCompany());
                    existingJob.setLocation(updatedJob.getLocation());
                    existingJob.setDescription(updatedJob.getDescription());
                    existingJob.setType(updatedJob.getType());
                    existingJob.setSalary(updatedJob.getSalary()); // 👈 updated
                    existingJob.setStatus(updatedJob.getStatus()); // 👈 updated

                    Job savedJob = jobRepository.save(existingJob);
                    return ResponseEntity.ok(savedJob);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{jobId}/cascade")
    public ResponseEntity<?> deleteJobCascade(@PathVariable Long jobId) {
        applicationRepository.deleteByJobId(jobId); // Delete related applications
        jobRepository.deleteById(jobId); // Delete the job
        return ResponseEntity.ok().build();
    }
}
