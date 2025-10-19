package com.example.ATS_Plus.Controller;

import com.example.ATS_Plus.Service.LocalLlamaCVScoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    private final LocalLlamaCVScoringService localLlamaCVScoringService;

    @Autowired
    public WebController(LocalLlamaCVScoringService localLlamaCVScoringService) {
        this.localLlamaCVScoringService = localLlamaCVScoringService;
    }

    @GetMapping("/")
    public String showInputForm(Model model) {
        // Check if local Llama model is available
        boolean llamaAvailable = localLlamaCVScoringService.isLlamaModelAvailable();
        model.addAttribute("llamaAvailable", llamaAvailable);
        return "input";
    }

    @GetMapping("/result")
    public String showResultPage() {
        return "result";
    }
}