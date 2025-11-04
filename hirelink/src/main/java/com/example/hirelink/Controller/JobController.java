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

    @GetMapping
    public List<Job> getJobs(@AuthenticationPrincipal User currentUser) {
        return jobRepository.findByEmployer(currentUser);
    }

    @PostMapping
    public Job createJob(@RequestBody Job job, @AuthenticationPrincipal User currentUser) {
        job.setEmployer(currentUser);
        return jobRepository.save(job);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return jobRepository.findById(id)
                .filter(job -> job.getEmployer().getId().equals(currentUser.getId())) // only allow owner
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Job> updateJob(@PathVariable Long id, @RequestBody Job updatedJob) {
        return jobRepository.findById(id)
                .map(job -> {
                    if (updatedJob.getTitle() != null) job.setTitle(updatedJob.getTitle());
                    if (updatedJob.getLocation() != null) job.setLocation(updatedJob.getLocation());
                    if (updatedJob.getType() != null) job.setType(updatedJob.getType());
                    if (updatedJob.getJobSalary() != null) job.setJobSalary(updatedJob.getJobSalary());
                    if (updatedJob.getJobStatus() != null) job.setJobStatus(updatedJob.getJobStatus());
                    return ResponseEntity.ok(jobRepository.save(job));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{jobId}/cascade")
    public ResponseEntity<?> deleteJobCascade(@PathVariable Long jobId, @AuthenticationPrincipal User currentUser) {
        return jobRepository.findById(jobId)
                .filter(job -> job.getEmployer().getId().equals(currentUser.getId())) // only allow owner
                .map(job -> {
                    applicationRepository.deleteByJobId(jobId); // Delete related applications
                    jobRepository.delete(job); // Delete the job
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
