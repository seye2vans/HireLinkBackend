package com.example.hirelink.Controller;

import com.example.hirelink.Job.Job;
import com.example.hirelink.Repositories.JobRepository;
import com.example.hirelink.Repositories.ApplicationRepository;
import com.example.hirelink.User.User;
import lombok.RequiredArgsConstructor;
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

    // Get all jobs for the authenticated employer
    @GetMapping
    public List<Job> getJobs(@AuthenticationPrincipal User currentUser) {
        return jobRepository.findByEmployer(currentUser);
    }

    // Create a new job
    @PostMapping
    public Job createJob(@RequestBody Job job, @AuthenticationPrincipal User currentUser) {
        job.setEmployer(currentUser);
        return jobRepository.save(job); // postedDate is auto-set
    }

    // Get a specific job by ID (owner only)
    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return jobRepository.findById(id)
                .filter(job -> job.getEmployer().getId().equals(currentUser.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update a job (owner only)
    @PatchMapping("/{id}")
    public ResponseEntity<Job> updateJob(@PathVariable Long id,
            @RequestBody Job updatedJob,
            @AuthenticationPrincipal User currentUser) {
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
                    return ResponseEntity.ok(jobRepository.save(job)); // updatedAt auto-set
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete a job and its related applications (owner only)
    @DeleteMapping("/{jobId}/cascade")
    public ResponseEntity<?> deleteJobCascade(@PathVariable Long jobId,
            @AuthenticationPrincipal User currentUser) {
        return jobRepository.findById(jobId)
                .filter(job -> job.getEmployer().getId().equals(currentUser.getId()))
                .map(job -> {
                    applicationRepository.deleteByJob_Id(jobId); // Delete related applications
                    jobRepository.delete(job); // Delete the job itself
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
