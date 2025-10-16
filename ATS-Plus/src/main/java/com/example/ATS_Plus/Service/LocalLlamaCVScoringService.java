package com.example.ATS_Plus.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.ATS_Plus.Service.PdfTextExtractor;
@Service
public class LocalLlamaCVScoringService {

    private final LocalLlamaService localLlamaService;
    private final LocalLlamaPdfService localLlamaPdfService;
    private final PdfTextExtractor pdfTextExtractor = new PdfTextExtractor();

    @Autowired
    public LocalLlamaCVScoringService(LocalLlamaService localLlamaService, LocalLlamaPdfService localLlamaPdfService) {
        this.localLlamaService = localLlamaService;
        this.localLlamaPdfService = localLlamaPdfService;
    }

    public String extractCvContent(String pdfUrl) {
        try {
            if (!localLlamaService.isModelAvailable()) {
                return "Error: Local Llama model is not available. Please ensure Ollama is running and the model is installed.";
            }

            return localLlamaPdfService.processPdfFromUrl(pdfUrl);
        } catch (Exception e) {
            return "Error processing the CV: " + e.getMessage();
        }
    }

    public String scoreCv(String cvContent, String jobRequirements) {
        try {
            if (!localLlamaService.isModelAvailable()) {
                return "Error: Local Llama model is not available. Please ensure Ollama is running and the model is installed.";
            }

            return localLlamaService.scoreCvAgainstJob(cvContent, jobRequirements);
        } catch (Exception e) {
            return "Error scoring CV: " + e.getMessage();
        }
    }

    public String getRawCvContent(String pdfUrl) {
        try {
            return localLlamaPdfService.getRawTextFromPdf(pdfUrl);
        } catch (Exception e) {
            return "Error extracting raw text from CV: " + e.getMessage();
        }
    }

    public boolean isLlamaModelAvailable() {
        return localLlamaService.isModelAvailable();
    }

}
