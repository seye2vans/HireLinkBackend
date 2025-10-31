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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    // ✅ Get applications by jobId & userEmail (for job seeker)
    @GetMapping
    public ResponseEntity<List<Application>> getUserApplications(
            @RequestParam Long jobId,
            @RequestParam String userEmail) {
        List<Application> apps = applicationRepository.findByJob_IdAndUser_Email(jobId, userEmail);
        return ResponseEntity.ok(apps);
    }

    // ✅ Submit application (from job seeker)
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

        // ✅ Save uploaded resume file (optional)
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

    // ✅ Get all applications for jobs posted by the employer
    @GetMapping("/employer")
    public ResponseEntity<List<Application>> getEmployerApplications(@AuthenticationPrincipal User employer) {
        // Fetch applications for jobs owned by this employer
        List<Application> applications = applicationRepository.findByJob_Employer(employer);
        return ResponseEntity.ok(applications);
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<Application> updateApplicationStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @AuthenticationPrincipal User employer) {

        return applicationRepository.findById(id)
                .filter(app -> app.getJob().getEmployer().getId().equals(employer.getId()))
                .map(app -> {
                    app.setStatus(status);
                    return ResponseEntity.ok(applicationRepository.save(app));
                })
                .orElse(ResponseEntity.notFound().build());
    }



}
