package com.resumeanalyzer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {
    
    @NotNull(message = "Resume file is required")
    private MultipartFile resumeFile;
    
    private String jobDescription;
    
}
