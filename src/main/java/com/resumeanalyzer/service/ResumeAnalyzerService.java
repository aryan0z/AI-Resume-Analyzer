package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.ResumeAnalysisResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.logging.Logger;

@Service
public class ResumeAnalyzerService {
    
    private static final Logger logger = Logger.getLogger(ResumeAnalyzerService.class.getName());
    
    private final PdfExtractionService pdfExtractionService;
    private final OpenRouterAnalysisService openRouterAnalysisService;
    
    public ResumeAnalyzerService(PdfExtractionService pdfExtractionService, 
                                OpenRouterAnalysisService openRouterAnalysisService) {
        this.pdfExtractionService = pdfExtractionService;
        this.openRouterAnalysisService = openRouterAnalysisService;
    }
    
    public ResumeAnalysisResponse analyzeResume(MultipartFile resumeFile) {
        logger.info("Starting complete resume analysis");
        long startTime = System.currentTimeMillis();
        
        try {
            // Step 1: Extract text from PDF
            String resumeText = pdfExtractionService.extractTextFromPdf(resumeFile);
            logger.info("PDF extraction completed. Text length: " + resumeText.length());
            
            // Step 2: Analyze with OpenRouter API
            ResumeAnalysisResponse response = openRouterAnalysisService.analyzeResume(resumeText);
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            
            return response;
            
        } catch (Exception e) {
            logger.severe("Error during resume analysis: " + e.getMessage());
            
            ResumeAnalysisResponse errorResponse = new ResumeAnalysisResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("Failed to analyze resume: " + e.getMessage());
            errorResponse.setProcessingTime(System.currentTimeMillis() - startTime);
            
            return errorResponse;
        }
    }
    
}
