package com.example.hirelink.Controller;

import com.example.hirelink.Job.Application;
import com.example.hirelink.Job.Job;
import com.example.hirelink.Repositories.ApplicationRepository;
import com.example.hirelink.Repositories.JobRepository;
import com.example.hirelink.Repositories.UserRepository;
import com.example.hirelink.User.User;
import com.example.hirelink.Security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // ✅ Get applications by jobId & userEmail
    @GetMapping
    public ResponseEntity<List<Application>> getUserApplications(
            @RequestParam Long jobId,
            @RequestParam String userEmail) {
        List<Application> apps = applicationRepository.findByJob_IdAndUser_Email(jobId, userEmail);
        return ResponseEntity.ok(apps);
    }

    // ✅ Submit application
    @PostMapping
    public ResponseEntity<Application> submitApplication(
            @RequestParam Long jobId,
            @RequestParam String coverLetter,
            @RequestParam(required = false) MultipartFile resume,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        Application app = new Application();
        app.setUser(user);                 // ✅ works now
        app.setJob(job);                   // ✅ works now
        app.setCoverLetter(coverLetter);   // ✅ works now

        if (resume != null && !resume.isEmpty()) {
            String filePath = "uploads/" + resume.getOriginalFilename();
            // Save file if needed:
            // resume.transferTo(new File(filePath));
            app.setResumePath(filePath);   // ✅ works now
        }

        applicationRepository.save(app);    // ✅ works now
        return ResponseEntity.ok(app);
    }

}
