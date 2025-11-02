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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // ✅ Allow frontend access
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
    public ResponseEntity<?> submitApplication(
            @RequestParam Long jobId,
            @RequestParam String coverLetter,
            @RequestParam(required = false) MultipartFile resume,
            @RequestHeader("Authorization") String authHeader) throws IOException {

        // Debug log to see incoming data
        System.out.println("📩 Application received:");
        System.out.println("jobId = " + jobId);
        System.out.println("coverLetter = " + coverLetter);
        System.out.println("resume = " + (resume != null ? resume.getOriginalFilename() : "none"));
        System.out.println("authHeader = " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("❌ Missing or invalid Authorization header");
        }

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // ✅ Prevent duplicate applications
        List<Application> existingApps = applicationRepository.findByJob_IdAndUser_Email(jobId, email);
        if (!existingApps.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ You already applied for this job");
        }

        Application app = new Application();
        app.setUser(user);
        app.setJob(job);
        app.setCoverLetter(coverLetter);

        // ✅ Optional resume upload
        if (resume != null && !resume.isEmpty()) {
            String uploadDir = "uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String filePath = uploadDir + resume.getOriginalFilename();
            resume.transferTo(new File(filePath));
            app.setResumePath(filePath);
        }

        Application savedApp = applicationRepository.save(app);
        System.out.println("✅ Application saved successfully for jobId: " + jobId);
        return ResponseEntity.ok(savedApp);
    }

    // ✅ Employer gets all job seekers’ applications for their own jobs
    @GetMapping("/employer")
    public ResponseEntity<List<Application>> getEmployerApplications(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);

        User employer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        List<Application> applications = applicationRepository.findByJob_Employer(employer);

        return ResponseEntity.ok(applications);
    }

    // ✅ Employer updates application status (Accept / Reject)
    @PostMapping
    public ResponseEntity<?> applyToJob(
            @RequestParam Long jobId,
            @RequestParam(required = false) String coverLetter,
            @AuthenticationPrincipal User user
    ) {
        try {
            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found"));

            Application application = new Application();
            application.setJob(job);
            application.setUser(user);
            application.setCoverLetter(coverLetter);
            application.setStatus("Under Review");
            application.setAppliedDate(LocalDateTime.now());
            applicationRepository.save(application);

            return ResponseEntity.ok(application);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Error applying: " + e.getMessage());
        }
    }

}
