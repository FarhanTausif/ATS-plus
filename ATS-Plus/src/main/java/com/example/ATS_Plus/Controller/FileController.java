package com.example.ATS_Plus.Controller;

import com.example.ATS_Plus.Model.CvContent;
import com.example.ATS_Plus.Model.CvFile;
import com.example.ATS_Plus.Model.JobRequirement;
import com.example.ATS_Plus.Model.User;
import com.example.ATS_Plus.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.ATS_Plus.Service.CvContentService;

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
    @Autowired
    private LocalLlamaPdfService localLlamaPdfService;
    @Autowired
    private CvContentService CvContentService;

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
            String cloudinaryUrl = cloudinaryService.uploadPdf(file, candidateName);
            CvFile cvFile = CvFile.builder()
                    .fileName(candidateName.trim())
                    .cloudinaryUrl(cloudinaryUrl)
                    .uploadDate(LocalDateTime.now())
                    .build();
            CvFile savedCvFile = cvFileService.saveCvFile(cvFile);
            String cvExtractedText = localLlamaPdfService.processPdfFromUrl(cvFile.getCloudinaryUrl());
            CvContent cvContent = CvContent.builder().cvContentId(cvFile.getCvFileId()).extractedText(cvExtractedText).build();
            CvContentService.saveCvContent(cvContent);
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
    public ResponseEntity<?> uploadJobRequirement(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("jobTitle") String jobTitle,
                                                  @RequestParam(value = "userId", defaultValue = "1") Long userId) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Please select a PDF file to upload"));
            }
            if (jobTitle == null || jobTitle.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Job title is required"));
            }
            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("User not found"));
            }
            String cloudinaryUrl = cloudinaryService.uploadPdf(file, jobTitle);
            String extractedDescription;
            try {
                extractedDescription = localLlamaPdfService.processPdfFromUrl(cloudinaryUrl);
                if (extractedDescription.startsWith("Error:")) {
                    return ResponseEntity.badRequest().body(createErrorResponse("Failed to extract text from PDF: " + extractedDescription));
                }
                if (extractedDescription.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(createErrorResponse("The uploaded PDF appears to be empty or contains no readable text"));
                }
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(createErrorResponse("Error processing PDF: " + e.getMessage()));
            }
            JobRequirement jobRequirement = JobRequirement.builder()
                    .jobTitle(jobTitle.trim())
                    .description(extractedDescription.trim())
                    .createdAt(LocalDateTime.now())
                    .user(user)
                    .build();
            JobRequirement savedJobRequirement = jobRequirementService.saveJobRequirement(jobRequirement);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Job requirement uploaded and processed successfully");
            response.put("jobRequirementId", savedJobRequirement.getJobRequirementId());
            response.put("jobTitle", savedJobRequirement.getJobTitle());
            response.put("description", savedJobRequirement.getDescription());
            response.put("cloudinaryUrl", cloudinaryUrl);
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
