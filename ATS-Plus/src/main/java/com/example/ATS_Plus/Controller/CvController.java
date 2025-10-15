package com.example.ATS_Plus.Controller;


import com.example.ATS_Plus.DTO.ScoringResult;
import com.example.ATS_Plus.Service.CVScoringService;
import com.example.ATS_Plus.Service.LocalLlamaCVScoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CvController {

    private final CVScoringService cvScoringService;
    private final LocalLlamaCVScoringService localLlamaCVScoringService;

    @Autowired
    public CvController(CVScoringService cvScoringService, LocalLlamaCVScoringService localLlamaCVScoringService) {
        this.cvScoringService = cvScoringService;
        this.localLlamaCVScoringService = localLlamaCVScoringService;
    }

    @GetMapping("/")
    public String showInputForm(Model model) {
        // Check if local Llama model is available
        boolean llamaAvailable = localLlamaCVScoringService.isLlamaModelAvailable();
        model.addAttribute("llamaAvailable", llamaAvailable);
        return "input";
    }

    @PostMapping("/score")
    public String scoreCv(@RequestParam("cvLink") String cvLink,
                          @RequestParam("jobLink") String jobLink,
                          Model model) {
        if (cvLink.isEmpty() || jobLink.isEmpty()) {
            model.addAttribute("error", "Please provide both the applicant CV and job requirements PDF links.");
            return "input";
        }

        try {
            // Extract content from both PDFs using Gemini
            String cvContent = cvScoringService.extractCvContent(cvLink);
            String jobContent = cvScoringService.extractCvContent(jobLink);

            // Score using Gemini (compare both texts)
            String score = cvScoringService.scoreCv(cvContent, jobContent);

            model.addAttribute("cvContent", cvContent);
            model.addAttribute("jobContent", jobContent);
            model.addAttribute("score", score);
            model.addAttribute("processingMethod", "Gemini AI");
            return "result";
        } catch (Exception e) {
            model.addAttribute("error", "Error processing the CV or Job Requirements: " + e.getMessage());
            return "input";
        }
    }

    @PostMapping("/score-local")
    public String scoreCvWithLocalLlama(@RequestParam("cvLink") String cvLink,
                                        @RequestParam("jobLink") String jobLink,
                                        Model model) {
        if (cvLink.isEmpty() || jobLink.isEmpty()) {
            model.addAttribute("error", "Please provide both the applicant CV and job requirements PDF links.");
            boolean llamaAvailable = localLlamaCVScoringService.isLlamaModelAvailable();
            model.addAttribute("llamaAvailable", llamaAvailable);
            return "input";
        }

        try {
            // Check if local Llama model is available
            if (!localLlamaCVScoringService.isLlamaModelAvailable()) {
                model.addAttribute("error", "Local Llama model is not available. Please ensure Ollama is running and llama3.2:3b model is installed.");
                model.addAttribute("llamaAvailable", false);
                return "input";
            }

            // Extract content from both PDFs using local Llama
//            String cvContent = localLlamaCVScoringService.extractCvContent(cvLink);
//            String jobContent = localLlamaCVScoringService.extractCvContent(jobLink);
//
//            // Check for errors in content extraction
//            if (cvContent.startsWith("Error:")) {
//                model.addAttribute("error", cvContent);
//                model.addAttribute("llamaAvailable", true);
//                return "input";
//            }
//            if (jobContent.startsWith("Error:")) {
//                model.addAttribute("error", jobContent);
//                model.addAttribute("llamaAvailable", true);
//                return "input";
//            }

            // Score using local Llama (compare both texts)
//            String score = localLlamaCVScoringService.scoreCv(cvContent, jobContent);
            ScoringResult result = localLlamaCVScoringService.scoreCvllama(, jobRequirements);
            model.addAttribute("summary", result.getSummary());
            model.addAttribute("score", result.getScore());
            model.addAttribute("pdfParseTimeMs", result.getPdfParseTimeMs());
            model.addAttribute("summarizeTimeMs", result.getSummarizeTimeMs());
            model.addAttribute("scoreTimeMs", result.getScoreTimeMs());
            model.addAttribute("cvContent", cvContent);
            model.addAttribute("jobContent", jobContent);
            model.addAttribute("score", score);
            model.addAttribute("processingMethod", "Local Llama 3.2 3B");
            return "result";
        } catch (Exception e) {
            model.addAttribute("error", "Error processing the CV or Job Requirements with local Llama: " + e.getMessage());
            model.addAttribute("llamaAvailable", localLlamaCVScoringService.isLlamaModelAvailable());
            return "input";
        }

        return "result";

    }
}