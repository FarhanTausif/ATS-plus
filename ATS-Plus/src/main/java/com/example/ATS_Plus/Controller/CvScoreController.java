package com.example.ATS_Plus.Controller;


import com.example.ATS_Plus.Model.CvFile;
import com.example.ATS_Plus.Model.JobRequirement;
import com.example.ATS_Plus.Service.CvFileService;
import com.example.ATS_Plus.Service.JobRequirementService;
import com.example.ATS_Plus.Service.LocalLlamaCVScoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CvScoreController {

    @Autowired
    private LocalLlamaCVScoringService localLlamaCVScoringService;

    @Autowired
    private CvFileService cvFileService;

    @Autowired
    private JobRequirementService jobRequirementService;

    @PostMapping("/scoreCv")
    public ResponseEntity<?> scoreCv(@RequestBody Map<String, String> request) {
        String cvFileIdStr = request.get("cvFileId");
        String jobRequirementIdStr = request.get("jobRequirementId");

        if(cvFileIdStr == null || cvFileIdStr.isEmpty() || jobRequirementIdStr == null || jobRequirementIdStr.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Please provide both CV file ID and job requirement ID.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            Long cvFileId = Long.parseLong(cvFileIdStr);
            Long jobRequirementId = Long.parseLong(jobRequirementIdStr);

            // Get uploaded files from database
            CvFile cvFile = cvFileService.getCvFileById(cvFileId);
            JobRequirement jobRequirement = jobRequirementService.getJobRequirementById(jobRequirementId);

            if (cvFile == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "CV file not found with ID: " + cvFileId);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (jobRequirement == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Job requirement not found with ID: " + jobRequirementId);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if(!localLlamaCVScoringService.isLlamaModelAvailable()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Local Llama model is not available. Please ensure Ollama is running and the model is installed.");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            long startExtract = System.currentTimeMillis();
            // Use Cloudinary URLs to extract content
            String cvContent = localLlamaCVScoringService.extractCvContent(cvFile.getCloudinaryUrl());
            String jobContent = jobRequirement.getDescription(); // Use description directly for job requirements
            long endExtract = System.currentTimeMillis();
            long extractTime = endExtract - startExtract;

            // Check for extraction errors
            if (cvContent.startsWith("Error:")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", cvContent);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            long startScore = System.currentTimeMillis();
            String score = localLlamaCVScoringService.scoreCv(cvContent, jobContent);
            long endScore = System.currentTimeMillis();
            long scoreTime = endScore - startScore;

            Map<String, Object> response = new HashMap<>();
            response.put("score", score);
            response.put("cvContent", cvContent);
            response.put("jobContent", jobContent);
            response.put("candidateName", cvFile.getFileName());
            response.put("jobTitle", jobRequirement.getJobTitle());
            response.put("processingMethod", "Local Llama 3.2 3B");
            response.put("extractTime", extractTime);
            response.put("scoreTime", scoreTime);
            response.put("totalTime", extractTime + scoreTime);

            return ResponseEntity.ok().body(response);
        } catch (NumberFormatException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid file ID format. Please provide valid numeric IDs.");
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error processing the CV or Job Requirements: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> checkStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("llamaAvailable", localLlamaCVScoringService.isLlamaModelAvailable());
        return ResponseEntity.ok(status);
    }
}
