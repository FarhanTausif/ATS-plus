package com.example.ATS_Plus.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.ATS_Plus.DTO.ScoringResult;
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
    public ScoringResult scoreCvllama(String pdfPath, String jobRequirements) {
        long startParse = System.currentTimeMillis();
        try {
            String cvContent = localLlamaPdfService.processPdfFromUrl(pdfPath);
            String jobContent = localLlamaPdfService.processPdfFromUrl(jobRequirements);
        }
        catch (Exception e) {
            ScoringResult errorResult = new ScoringResult();
            errorResult.setSummary("Error processing the pdfs: " + e.getMessage());
            return errorResult;
        }

        long endParse = System.currentTimeMillis();

        long startSummarize = System.currentTimeMillis();
        String summary = localLlamaService.summarizeText(cvText);
        long endSummarize = System.currentTimeMillis();

        long startScore = System.currentTimeMillis();
        String score = localLlamaService.scoreCvAgainstJob(cvText, jobRequirements);
        long endScore = System.currentTimeMillis();

        ScoringResult result = new ScoringResult();
        result.setSummary(summary);
        result.setScore(score);
        result.setPdfParseTimeMs(endParse - startParse);
        result.setSummarizeTimeMs(endSummarize - startSummarize);
        result.setScoreTimeMs(endScore - startScore);
        return result;
    }

}
