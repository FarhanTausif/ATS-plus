package com.example.ATS_Plus.Model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "cv_content")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CvContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cvContentId")
    private Long cvContentId;
    @Column(name="cvFileId", nullable = false)
    private Long cvFileId;
    @Column(name="extractedText", nullable = false)
    private String extractedText;
}
