package com.example.ATS_Plus.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;


@Service
public class GeminiPdfSummarizerService {

    private final WebClient webClient;
    private final String apiKey;

    public GeminiPdfSummarizerService(@Value("${gemini.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    public String summarizePdfFromUrl(String pdfUrl) throws IOException {
        byte[] pdfBytes;
        try {
            pdfBytes = downloadPdf(pdfUrl);
        } catch (IOException e) {
            return "Error: Could not download PDF (" + e.getMessage() + ")";
        }
        String pdfText;
        try {
            pdfText = extractTextFromPdf(pdfBytes);
        } catch (Exception e) {
            return "Error: The file is not a valid or readable PDF (" + e.getMessage() + ")";
        }
        String requestBody = """
        {
          "contents": [
            {
              "parts": [
                {
                  "text": "%s"
                },
                {
                  "text": "Summarize this document"
                }
              ]
            }
          ]
        }
        """.formatted(pdfText.replace("\"", "\\\""));

        return webClient.post()
                .uri("/models/gemini-2.5-flash:generateContent?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String extractTextFromPdf(byte[] pdfBytes) throws IOException {
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private byte[] downloadPdf(String pdfUrl) throws IOException {
        String directUrl = pdfUrl;
        // Handle Google Drive share links
        if (pdfUrl.contains("drive.google.com")) {
            String fileId = null;
            // Match /file/d/FILE_ID/ or id=FILE_ID
            var matcher = java.util.regex.Pattern.compile("/file/d/([a-zA-Z0-9_-]+)").matcher(pdfUrl);
            if (matcher.find()) {
                fileId = matcher.group(1);
            } else if (pdfUrl.contains("id=")) {
                int idx = pdfUrl.indexOf("id=") + 3;
                int end = pdfUrl.indexOf('&', idx);
                fileId = end > idx ? pdfUrl.substring(idx, end) : pdfUrl.substring(idx);
            }
            if (fileId != null) {
                directUrl = "https://drive.google.com/uc?export=download&id=" + fileId;
            } else {
                throw new IOException("Could not extract file ID from Google Drive link. Please provide a valid share link.");
            }
        }
        var connection = (HttpURLConnection) new URL(directUrl).openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        int responseCode = connection.getResponseCode();
        String contentType = connection.getContentType();
        if (responseCode != 200) {
            throw new IOException("HTTP response code: " + responseCode);
        }
        try (var inputStream = connection.getInputStream()) {
            byte[] fileBytes = inputStream.readAllBytes();
            // Accept application/pdf and application/octet-stream
            boolean isPdfContentType = contentType != null && (contentType.toLowerCase().contains("pdf") || contentType.toLowerCase().contains("octet-stream"));
            boolean isPdfHeader = fileBytes.length > 4 && new String(fileBytes, 0, 5).equals("%PDF-");
            if (!isPdfContentType || !isPdfHeader) {
                throw new IOException("Content-Type is not PDF or file does not start with %PDF-: " + contentType + ". If this is a Google Drive link, make sure the file is shared publicly and the link is correct.");
            }
            return fileBytes;
        }
    }
}
