package com.example.hirelink.Controller;

import com.example.hirelink.Job.Application;
import com.example.hirelink.Job.Job;
import com.example.hirelink.Repositories.ApplicationRepository;
import com.example.hirelink.Repositories.JobRepository;
import com.example.hirelink.Repositories.UserRepository;
import com.example.hirelink.Security.JwtUtil;
import com.example.hirelink.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // ✅ Job seeker gets their applications for a specific job
    @GetMapping
    public ResponseEntity<List<Application>> getUserApplications(
            @RequestParam Long jobId,
            @RequestParam String userEmail) {
        List<Application> apps = applicationRepository.findByJob_IdAndUser_Email(jobId, userEmail);
        return ResponseEntity.ok(apps);
    }

    // ✅ Job seeker submits application
    @PostMapping
    public ResponseEntity<Application> submitApplication(
            @RequestParam Long jobId,
            @RequestParam String coverLetter,
            @RequestParam(required = false) MultipartFile resume,
            @RequestHeader("Authorization") String authHeader) throws IOException {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        Application app = new Application();
        app.setUser(user);
        app.setJob(job);
        app.setCoverLetter(coverLetter);

        // Optional resume upload
        if (resume != null && !resume.isEmpty()) {
            String uploadDir = "uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String filePath = uploadDir + resume.getOriginalFilename();
            resume.transferTo(new File(filePath));
            app.setResumePath(filePath);
        }

        applicationRepository.save(app);
        return ResponseEntity.ok(app);
    }

    // ✅ Employer gets all job seekers’ applications for their own jobs
    @GetMapping("/employer")
    public ResponseEntity<List<Application>> getEmployerApplications(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);

        User employer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        List<Application> applications = applicationRepository.findByJob_Employer(employer);
        return ResponseEntity.ok(applications);
    }

    // ✅ Employer updates application status (Accept / Reject)
    @PatchMapping("/{id}/status")
    public ResponseEntity<Application> updateApplicationStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);

        User employer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        return applicationRepository.findById(id)
                .filter(app -> app.getJob().getEmployer().getId().equals(employer.getId()))
                .map(app -> {
                    app.setStatus(status);
                    return ResponseEntity.ok(applicationRepository.save(app));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
