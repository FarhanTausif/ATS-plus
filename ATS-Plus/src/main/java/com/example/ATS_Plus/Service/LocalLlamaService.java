package com.example.ATS_Plus.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class LocalLlamaService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String llamaApiUrl;
    private final String modelName;

    public LocalLlamaService(@Value("${llama.api.url:http://localhost:11434}") String llamaApiUrl,
                             @Value("${llama.model.name:llama3.2:3b}") String modelName) {
        this.llamaApiUrl = llamaApiUrl;
        this.modelName = modelName;
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder()
                .baseUrl(llamaApiUrl)
                .build();
    }

    public String generateResponse(String prompt) {
        try {
            String requestBody = String.format("""
                {
                    "model": "%s",
                    "prompt": "%s",
                    "stream": false,
                    "options": {
                        "temperature": 0.1,
                        "top_p": 0.9,
                        "max_tokens": 2000
                    }
                }
                """, modelName, escapeJson(prompt));

            String response = webClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMinutes(5))
                    .block();

            return extractResponseFromJson(response);

        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return "Error: Llama model not found. Please ensure Ollama is running and the model '" + modelName + "' is installed.";
            }
            return "Error communicating with local Llama model: " + e.getMessage();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String summarizeText(String text) {
        String prompt = String.format("""
            Please summarize the following document content in a clear and concise manner. 
            Focus on key information, skills, experience, and qualifications mentioned:
            
            %s
            
            Summary:
            """, text);

        return generateResponse(prompt);
    }

    public String scoreCvAgainstJob(String cvContent, String jobRequirements) {
        String prompt = String.format("""
            Act as an HR Manager with 20 years of experience.
            Compare the resume content provided below with the required job skills given below.
            Check for key skills in the resume that match the job skills.
            Rate the resume out of 100 based on the matching skill set.
            Assess the score with high accuracy.
            
            Resume content: %s
            
            Job requirements: %s
            
            Respond only with the score in the format: Score: X/100
            """, cvContent, jobRequirements);

        return generateResponse(prompt);
    }

    private String extractResponseFromJson(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            if (rootNode.has("response")) {
                return rootNode.get("response").asText();
            }
            return "Error: Unexpected response format from Llama model";
        } catch (Exception e) {
            return "Error parsing Llama response: " + e.getMessage();
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    public boolean isModelAvailable() {
        try {
            webClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
