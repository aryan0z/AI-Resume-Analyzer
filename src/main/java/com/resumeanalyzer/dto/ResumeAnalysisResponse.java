package com.resumeanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeAnalysisResponse {
    
    private int overallScore;
    private int atsScore;
    
    private List<String> technicalSkills;
    private List<String> softSkills;
    
    private List<String> missingSkills;
    private List<String> improvementSuggestions;
    
    private List<String> suitableJobRoles;
    private List<String> industryMatches;
    
    private String summary;
    private String strengths;
    private String weaknesses;
    
    private Map<String, Object> detailedAnalysis;
    
    private long processingTime;
    private boolean success;
    private String errorMessage;
    
}
