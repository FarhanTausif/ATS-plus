package com.example.ATS_Plus.Controller;

import com.example.ATS_Plus.Model.CvFile;
import com.example.ATS_Plus.Model.JobRequirement;
import com.example.ATS_Plus.Model.User;
import com.example.ATS_Plus.Service.CloudinaryService;
import com.example.ATS_Plus.Service.CvFileService;
import com.example.ATS_Plus.Service.JobRequirementService;
import com.example.ATS_Plus.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private CvFileService cvFileService;

    @Autowired
    private JobRequirementService jobRequirementService;

    @Autowired
    private UserService userService;

    @PostMapping("/cv")
    public ResponseEntity<?> uploadCv(@RequestParam("file") MultipartFile file,
                                      @RequestParam("candidateName") String candidateName) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Please select a PDF file to upload"));
            }

            if (candidateName == null || candidateName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Candidate name is required"));
            }

            // Upload to Cloudinary
            String cloudinaryUrl = cloudinaryService.uploadPdf(file, candidateName);

            // Save to database
            CvFile cvFile = CvFile.builder()
                    .fileName(candidateName.trim())
                    .cloudinaryUrl(cloudinaryUrl)
                    .uploadDate(LocalDateTime.now())
                    .build();

            CvFile savedCvFile = cvFileService.saveCvFile(cvFile);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "CV uploaded successfully");
            response.put("cvFileId", savedCvFile.getCvFileId());
            response.put("fileName", savedCvFile.getFileName());
            response.put("cloudinaryUrl", savedCvFile.getCloudinaryUrl());
            response.put("uploadDate", savedCvFile.getUploadDate());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Error uploading CV: " + e.getMessage()));
        }
    }

    @PostMapping("/job-requirement")
    public ResponseEntity<?> uploadJobRequirement(@RequestParam(value = "file", required = true) MultipartFile file,
                                                  @RequestParam("jobTitle") String jobTitle,
                                                  @RequestParam("description") String description,
                                                  @RequestParam(value = "userId", defaultValue ="1" ) Long userId) {
        try {
            if (jobTitle == null || jobTitle.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Job title is required"));
            }

            if (description == null || description.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Job description is required"));
            }

            // Get user (HR) - for now using a default user, you can implement proper authentication
            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("User not found"));
            }

            String cloudinaryUrl = null;

            // Only upload to Cloudinary if file is provided
            if (file != null && !file.isEmpty()) {
                cloudinaryUrl = cloudinaryService.uploadPdf(file, jobTitle);
            }

            // Save job requirement to database
            JobRequirement jobRequirement = JobRequirement.builder()
                    .jobTitle(jobTitle.trim())
                    .description(description.trim())
                    .createdAt(LocalDateTime.now())
                    .user(user)
                    .build();

            JobRequirement savedJobRequirement = jobRequirementService.saveJobRequirement(jobRequirement);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Job requirement uploaded successfully");
            response.put("jobRequirementId", savedJobRequirement.getJobRequirementId());
            response.put("jobTitle", savedJobRequirement.getJobTitle());
            response.put("description", savedJobRequirement.getDescription());
            if (cloudinaryUrl != null) {
                response.put("cloudinaryUrl", cloudinaryUrl);
            }
            response.put("createdAt", savedJobRequirement.getCreatedAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Error uploading job requirement: " + e.getMessage()));
        }
    }

    @GetMapping("/cv/list")
    public ResponseEntity<?> listCvFiles() {
        try {
            return ResponseEntity.ok(cvFileService.getAllCvFiles());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Error fetching CV files: " + e.getMessage()));
        }
    }

    @GetMapping("/job-requirement/list")
    public ResponseEntity<?> listJobRequirements() {
        try {
            return ResponseEntity.ok(jobRequirementService.getAllJobRequirements());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Error fetching job requirements: " + e.getMessage()));
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        return error;
    }
}
