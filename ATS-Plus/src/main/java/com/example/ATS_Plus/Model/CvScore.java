package com.example.ATS_Plus.Model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cv_score")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CvScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cvScoreId")
    private Long cvScoreId;

    @Column(name = "score", nullable = false)
    private Double score;
    @Column (name="cvFileId", nullable = false)
    private Long cvFileId;

    @Column(name="skillMatched", length = 2000)
    private String skillMatched;

    @Column(name="skillLacking", length = 2000)
    private String skillLacking;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobRequirementId", nullable = false)
    private JobRequirement jobRequirement;
}
