package com.example.hirelink.Controller;

import com.example.hirelink.Job.Job;
import com.example.hirelink.Repositories.JobRepository;
import com.example.hirelink.Repositories.ApplicationRepository;
import com.example.hirelink.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    // Get all jobs for the current user (employer)
    @GetMapping
    public ResponseEntity<List<Job>> getJobs(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Job> jobs = jobRepository.findByEmployerId(currentUser.getId());
        return ResponseEntity.ok(jobs);
    }

    // Create a new job
    @PostMapping
    public ResponseEntity<Job> createJob(@RequestBody Job job, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        job.setEmployer(currentUser);
        Job savedJob = jobRepository.save(job);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedJob);
    }

    // Get job by ID (only if current user is the employer)
    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return jobRepository.findById(id)
                .filter(job -> job.getEmployer().getId().equals(currentUser.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Update job by ID
    @PatchMapping("/{id}")
    public ResponseEntity<Job> updateJob(@PathVariable Long id,
            @RequestBody Job updatedJob,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return jobRepository.findById(id)
                .filter(job -> job.getEmployer().getId().equals(currentUser.getId()))
                .map(job -> {
                    if (updatedJob.getTitle() != null)
                        job.setTitle(updatedJob.getTitle());
                    if (updatedJob.getLocation() != null)
                        job.setLocation(updatedJob.getLocation());
                    if (updatedJob.getType() != null)
                        job.setType(updatedJob.getType());
                    if (updatedJob.getJobSalary() != null)
                        job.setJobSalary(updatedJob.getJobSalary());
                    if (updatedJob.getJobStatus() != null)
                        job.setJobStatus(updatedJob.getJobStatus());
                    Job savedJob = jobRepository.save(job);
                    return ResponseEntity.ok(savedJob);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Delete job and its related applications
    @DeleteMapping("/{jobId}/cascade")
    public ResponseEntity<Void> deleteJobCascade(@PathVariable Long jobId, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return jobRepository.findById(jobId)
                .filter(job -> job.getEmployer().getId().equals(currentUser.getId()))
                .map(job -> {
                    applicationRepository.deleteByJob_Id(jobId); // Delete related applications
                    jobRepository.delete(job); // Delete the job
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
