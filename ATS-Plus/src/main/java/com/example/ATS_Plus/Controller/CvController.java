package com.example.ATS_Plus.Controller;


import com.example.ATS_Plus.Service.CVScoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CvController {

    private final CVScoringService cvScoringService;

    @Autowired
    public CvController(CVScoringService cvScoringService) {
        this.cvScoringService = cvScoringService;
    }

    @GetMapping("/")
    public String showInputForm() {
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
            // Extract content from both PDFs
            String cvContent = cvScoringService.extractCvContent(cvLink);
            String jobContent = cvScoringService.extractCvContent(jobLink);

            // Score using Gemini (compare both texts)
            String score = cvScoringService.scoreCv(cvContent, jobContent);

            model.addAttribute("cvContent", cvContent);
            model.addAttribute("jobContent", jobContent);
            model.addAttribute("score", score);
            return "result";
        } catch (Exception e) {
            model.addAttribute("error", "Error processing the CV or Job Requirements: " + e.getMessage());
            return "input";
        }
    }
}