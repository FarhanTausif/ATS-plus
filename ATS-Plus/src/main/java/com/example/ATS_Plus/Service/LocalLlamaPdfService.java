package com.example.ATS_Plus.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class LocalLlamaPdfService {

    private final LocalLlamaService localLlamaService;

    @Autowired
    public LocalLlamaPdfService(LocalLlamaService localLlamaService) {
        this.localLlamaService = localLlamaService;
    }

    public String processPdfFromUrl(String pdfUrl) throws IOException {
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

        if (pdfText.trim().isEmpty()) {
            return "Error: PDF appears to be empty or contains no readable text";
        }

//        return localLlamaService.summarizeText(pdfText);
        return pdfText;
    }

    private String extractTextFromPdf(byte[] pdfBytes) throws IOException {
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private byte[] downloadPdf(String pdfUrl) throws IOException {
        String directUrl = pdfUrl;

        if (pdfUrl.contains("drive.google.com")) {
            String fileId = null;
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
        connection.setReadTimeout(30000); // Increased timeout for larger files
        
        int responseCode = connection.getResponseCode();
        String contentType = connection.getContentType();
        
        if (responseCode != 200) {
            throw new IOException("HTTP response code: " + responseCode);
        }

        if (contentType != null && !contentType.toLowerCase().contains("pdf")) {
            if (contentType.toLowerCase().contains("text/html")) {
                throw new IOException("Received HTML instead of PDF. The URL might be a preview link rather than a direct download link.");
            }
        }

        return connection.getInputStream().readAllBytes();
    }

    public String getRawTextFromPdf(String pdfUrl) throws IOException {
        byte[] pdfBytes = downloadPdf(pdfUrl);
        return extractTextFromPdf(pdfBytes);
    }
}
