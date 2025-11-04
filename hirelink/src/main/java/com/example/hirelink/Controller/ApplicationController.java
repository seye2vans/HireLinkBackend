package com.example.hirelink.Controller;

import com.example.hirelink.Application.Application;
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
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ApplicationController {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // ✅ Apply for a job
    @PostMapping
    public ResponseEntity<?> submitApplication(
            @RequestParam Long jobId,
            @RequestParam(required = false) String coverLetter,
            @RequestParam(required = false) MultipartFile resume,
            @RequestHeader("Authorization") String authHeader
    ) throws IOException {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("❌ Missing or invalid Authorization header");
        }

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);

        User applicant = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // ✅ Prevent duplicate applications
        List<Application> existing = applicationRepository.findByJob_IdAndApplicant_Email(jobId, email);
        if (!existing.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ You already applied for this job");
        }

        Application app = new Application();
        app.setApplicant(applicant);
        app.setJob(job);
        app.setStatus("Under Review");
        app.setResumeUrl(null);
        app.setCoverLetter(coverLetter);
        app.setAppliedDate(LocalDateTime.now());

        if (resume != null && !resume.isEmpty()) {
            String uploadDir = "uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String filePath = uploadDir + resume.getOriginalFilename();
            resume.transferTo(new File(filePath));
            app.setResumeUrl(filePath);
        }

        Application saved = applicationRepository.save(app);
        return ResponseEntity.ok(saved);
    }

    // ✅ Employer fetch applications
    @GetMapping("/employer")
    public ResponseEntity<List<Application>> getEmployerApplications(
            @RequestHeader("Authorization") String authHeader
    ) {
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
}
