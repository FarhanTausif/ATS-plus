package com.example.ATS_Plus.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(@Value("${cloudinary.cloud-name}") String cloudName,
                           @Value("${cloudinary.api-key}") String apiKey,
                           @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    public String uploadPdf(MultipartFile file, String fileName) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new IOException("Only PDF files are allowed");
        }

        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "resource_type", "raw",
                "public_id", "ats-plus/pdfs/" + System.currentTimeMillis() + "_" + fileName,
                "use_filename", true,
                "unique_filename", false,
                "folder", "ats-plus/pdfs"
        );

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
        return (String) uploadResult.get("secure_url");
    }

    public void deletePdf(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw"));
    }

    public String extractPublicIdFromUrl(String cloudinaryUrl) {
        if (cloudinaryUrl != null && cloudinaryUrl.contains("/")) {
            String[] parts = cloudinaryUrl.split("/");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("raw") && i + 2 < parts.length) {
                    return parts[i + 2].split("\\.")[0]; // Remove file extension
                }
            }
        }
        return null;
    }
}
