package com.example.ATS_Plus.Model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name ="cv_file")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CvFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cvFileId")
    private Long cvFileId;

    @Column(name = "fileName", nullable = false)
    private String fileName;

    @Column(name = "cloudinaryUrl", nullable = false)
    private String cloudinaryUrl;

    @Column(name = "uploadDate",columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = false)
    private LocalDateTime uploadDate;
}
