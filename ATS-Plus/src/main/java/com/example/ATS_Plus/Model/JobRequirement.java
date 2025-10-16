package com.example.ATS_Plus.Model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "job_requirement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRequirement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "jobRequirementId")
    private Long jobRequirementId;
    @Column(name = "jobTitle", nullable = false)
    private String jobTitle;
    @Column(name = "description", nullable = false, length = 3000)
    private String description;
    @Column(
            name = "createdAt",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
            nullable = false
    )
    private LocalDateTime createdAt;
    // Many-to-One relationship with User (HR)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;
    // One-to-Many relationship with CvScore
//    @OneToMany(mappedBy = "jobRequirement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<CvScore> cvScores;
}
