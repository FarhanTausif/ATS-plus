package com.example.ATS_Plus.Controller;

import com.example.ATS_Plus.Service.LocalLlamaCVScoringService;
import com.example.ATS_Plus.Service.JobRequirementService;
import com.example.ATS_Plus.Model.JobRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class WebController {

    private final LocalLlamaCVScoringService localLlamaCVScoringService;
    private final JobRequirementService jobRequirementService;

    @Autowired
    public WebController(LocalLlamaCVScoringService localLlamaCVScoringService, JobRequirementService jobRequirementService) {
        this.localLlamaCVScoringService = localLlamaCVScoringService;
        this.jobRequirementService = jobRequirementService;
    }

    @GetMapping("/")
    public String showInputForm(Model model) {
        boolean llamaAvailable = localLlamaCVScoringService.isLlamaModelAvailable();
        model.addAttribute("llamaAvailable", llamaAvailable);

        // Add real job data to the home page
        model.addAttribute("jobs", jobRequirementService.getAllJobRequirements());

        return "home";
    }

    @GetMapping("/result")
    public String showResultPage() {
        return "result";
    }

    @GetMapping("/job-details/{id}")
    public String jobDetails(@PathVariable Long id, Model model) {
        JobRequirement job = jobRequirementService.getJobRequirementById(id);
        if (job == null) {
            return "redirect:/";
        }
        model.addAttribute("job", job);
        return "job-details";
    }

    @GetMapping("/candidate/job-search")
    public String candidateJobSearch(Model model) {
        // Add real job data for candidates to browse
        model.addAttribute("jobs", jobRequirementService.getAllJobRequirements());
        return "candidate/job-search";
    }

    @GetMapping("/hr/dashboard")
    public String hrDashboard(Model model) {
        // Get dashboard statistics
        List<JobRequirement> allJobs = jobRequirementService.getAllJobRequirements();
        model.addAttribute("totalJobs", allJobs.size());

        // Mock data for other statistics (can be replaced with real data later)
        model.addAttribute("totalApplications", 45);
        model.addAttribute("pendingInterviews", 12);
        model.addAttribute("newCandidates", 8);

        // Mock recent activities (can be replaced with real data later)
        model.addAttribute("recentActivities", createMockActivities());

        return "hr/dashboard";
    }

    private java.util.List<java.util.Map<String, String>> createMockActivities() {
        java.util.List<java.util.Map<String, String>> activities = new java.util.ArrayList<>();

        java.util.Map<String, String> activity1 = new java.util.HashMap<>();
        activity1.put("title", "New job application received");
        activity1.put("description", "John Doe applied for Senior Developer position");
        activity1.put("timestamp", "2 hours ago");
        activities.add(activity1);

        java.util.Map<String, String> activity2 = new java.util.HashMap<>();
        activity2.put("title", "Interview scheduled");
        activity2.put("description", "Interview with Sarah Smith for UI/UX Designer role");
        activity2.put("timestamp", "4 hours ago");
        activities.add(activity2);

        java.util.Map<String, String> activity3 = new java.util.HashMap<>();
        activity3.put("title", "New job posted");
        activity3.put("description", "Backend Engineer position published");
        activity3.put("timestamp", "1 day ago");
        activities.add(activity3);

        return activities;
    }

    @GetMapping("/hr/job-management")
    public String hrJobManagement(Model model) {
        return "hr/job-management";
    }
}