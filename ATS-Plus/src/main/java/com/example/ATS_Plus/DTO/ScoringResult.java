package com.example.ATS_Plus.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoringResult {
    private String summary;
    private String score;
    private long pdfParseTimeMs;
    private long summarizeTimeMs;
    private long scoreTimeMs;
}
