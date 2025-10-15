package com.example.ATS_Plus.Service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CVScoringService {

    private final ChatClient chatClient;
    private final GeminiPdfSummarizerService geminiPdfSummarizerService;

    @Autowired
    public CVScoringService(ChatClient.Builder chatClientBuilder, GeminiPdfSummarizerService geminiPdfSummarizerService) {
        this.chatClient = chatClientBuilder.build();
        this.geminiPdfSummarizerService = geminiPdfSummarizerService;
    }

    public String extractCvContent(String pdfUrl) {
        try {

 return geminiPdfSummarizerService.summarizePdfFromUrl(pdfUrl);
        } catch (Exception e) {
            return "Error processing the CV: " + e.getMessage();
        }
    }

    public String scoreCv(String cvContent, String jobSkills) {
        String scoringPrompt = String.format(
                """
                Act as an HR Manager with 20 years of experience.
                Compare the resume content provided below with the required job skills given below.
                Check for key skills in the resume that match the job skills.
                Rate the resume out of 100 based on the matching skill set.
                Assess the score with high accuracy.
                Resume content: %s
                Job skills: %s
                Respond only with the score in the format: Score: X/100
                """,
                cvContent, jobSkills
        );
        Prompt prompt = new Prompt(new UserMessage(scoringPrompt));
        return chatClient.prompt(prompt).call().content();
    }
}