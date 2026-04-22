package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.ResumeAnalysisResponse;
import com.resumeanalyzer.exception.FileProcessingException;
import com.resumeanalyzer.service.ResumeAnalyzerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
public class ResumeAnalyzerController {
    
    private static final Logger logger = Logger.getLogger(ResumeAnalyzerController.class.getName());
    private final ResumeAnalyzerService resumeAnalyzerService;
    
    public ResumeAnalyzerController(ResumeAnalyzerService resumeAnalyzerService) {
        this.resumeAnalyzerService = resumeAnalyzerService;
    }
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @PostMapping("/api/analyze")
    @ResponseBody
    public ResponseEntity<?> analyzeResume(@RequestParam("file") MultipartFile file) {
        logger.info("Received resume analysis request");
        
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("File is empty or not provided"));
            }
            
            ResumeAnalysisResponse response = resumeAnalyzerService.analyzeResume(file);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (FileProcessingException e) {
            logger.warning("File processing error: " + e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
            
        } catch (Exception e) {
            logger.severe("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An unexpected error occurred: " + e.getMessage()));
        }
    }
    
    @GetMapping("/api/health")
    @ResponseBody
    public ResponseEntity<?> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "AI Resume Analyzer");
        return ResponseEntity.ok(response);
    }
    
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("success", "false");
        error.put("errorMessage", message);
        return error;
    }
    
}
