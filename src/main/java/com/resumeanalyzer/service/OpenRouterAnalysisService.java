package com.resumeanalyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeanalyzer.dto.ResumeAnalysisResponse;
import com.resumeanalyzer.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Service for analyzing resumes using OpenRouter API.
 * Uses LLaMA 3.1 8B model or fallback to other free models.
 * Implements dynamic fallback analysis based on resume text if API fails.
 */
@Service
public class OpenRouterAnalysisService {
    
    private static final Logger logger = Logger.getLogger(OpenRouterAnalysisService.class.getName());
    private static final String OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions";
    
    @Value("${openrouter.api.key}")
    private String apiKey;
    
    @Value("${openrouter.api.model:meta-llama/llama-3.1-8b-instruct:free}")
    private String model;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public OpenRouterAnalysisService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Analyzes a resume using OpenRouter API with fallback to dynamic analysis.
     * 
     * @param resumeText Extracted resume text from PDF
     * @return ResumeAnalysisResponse with analysis results
     */
    public ResumeAnalysisResponse analyzeResume(String resumeText) {
        logger.info("Starting resume analysis with OpenRouter API");
        long startTime = System.currentTimeMillis();
        
        try {
            if (resumeText == null || resumeText.trim().isEmpty()) {
                throw new ApiException("Resume text is empty");
            }
            
            // Step 1: Try to call OpenRouter API
            try {
                String analysisPrompt = buildAnalysisPrompt(resumeText);
                String apiResponse = callOpenRouterApi(analysisPrompt);
                
                ResumeAnalysisResponse response = parseAnalysisResponse(apiResponse);
                response.setProcessingTime(System.currentTimeMillis() - startTime);
                response.setSuccess(true);
                
                logger.info("Resume analysis completed successfully via OpenRouter API in " + response.getProcessingTime() + "ms");
                return response;
            } catch (ApiException apiError) {
                logger.severe("OpenRouter API failed (quota/timeout/exception): " + apiError.getMessage());
                // Fall through to dynamic fallback
            }
            
            // Step 2: Use dynamic fallback based on resume text
            logger.info("Using dynamic fallback analysis based on resume content");
            ResumeAnalysisResponse fallbackResponse = createDynamicFallbackResponse(resumeText, System.currentTimeMillis() - startTime);
            return fallbackResponse;
            
        } catch (Exception e) {
            logger.severe("Unexpected error during analysis: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("Failed to analyze resume: " + e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }
    
    /**
     * Builds the analysis prompt for the AI model.
     * Instructs the model to return structured clean text analysis.
     */
    private String buildAnalysisPrompt(String resumeText) {
        return """
                You are an expert resume analyst and recruiter. Analyze the following resume professionally and provide comprehensive feedback.
                
                Return a structured analysis in this EXACT format with clear labels:
                
                OVERALL SCORE: [0-100 integer]
                ATS SCORE: [0-100 integer]
                
                TECHNICAL SKILLS:
                - [skill 1]
                - [skill 2]
                - [skill 3]
                (list all technical skills found)
                
                MISSING SKILLS:
                - [skill 1]
                - [skill 2]
                - [skill 3]
                - [skill 4]
                - [skill 5]
                
                PROFESSIONAL SUMMARY:
                [2-3 sentence professional summary]
                
                STRENGTHS:
                [Detailed strengths paragraph]
                
                IMPROVEMENT SUGGESTIONS:
                - [suggestion 1]
                - [suggestion 2]
                - [suggestion 3]
                - [suggestion 4]
                - [suggestion 5]
                
                SUITABLE JOB ROLES:
                - [role 1]
                - [role 2]
                - [role 3]
                - [role 4]
                - [role 5]
                
                RESUME CONTENT TO ANALYZE:
                """ + resumeText;
    }
    
    /**
     * Calls the OpenRouter API with the analysis prompt.
     * Uses Chat Completions API format compatible with OpenAI.
     */
    private String callOpenRouterApi(String prompt) {
        try {
            logger.info("Calling OpenRouter API with model: " + model);
            
            // Build request body for OpenRouter (OpenAI-compatible format)
            Map<String, Object> requestBody = buildOpenRouterRequestBody(prompt);
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("User-Agent", "AI-Resume-Analyzer/2.0");
            headers.set("HTTP-Referer", "https://github.com/resumeanalyzer");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // Call OpenRouter API
            ResponseEntity<Map> response = restTemplate.postForEntity(OPENROUTER_API_URL, entity, Map.class);
            
            // Check response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("OpenRouter API returned successfully");
                return extractTextFromOpenRouterResponse(response.getBody());
            }
            
            throw new ApiException("OpenRouter API returned status: " + response.getStatusCode());
            
        } catch (RestClientException e) {
            logger.severe("REST client error calling OpenRouter API: " + e.getMessage());
            throw new ApiException("Failed to connect to OpenRouter API: " + e.getMessage(), e);
        } catch (ApiException e) {
            logger.severe("OpenRouter API error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.severe("Unexpected error calling OpenRouter API: " + e.getMessage());
            throw new ApiException("Failed to call OpenRouter API: " + e.getMessage(), e);
        }
    }
    
    /**
     * Builds the request body for OpenRouter Chat Completions API.
     * Compatible with OpenAI's API format.
     */
    private Map<String, Object> buildOpenRouterRequestBody(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        
        // Set model
        requestBody.put("model", model);
        
        // Build messages
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        
        requestBody.put("messages", messages);
        
        // Temperature for consistency
        requestBody.put("temperature", 0.7);
        
        // Max tokens for response
        requestBody.put("max_tokens", 2000);
        
        return requestBody;
    }
    
    /**
     * Extracts the response text from OpenRouter API response.
     * OpenRouter uses OpenAI-compatible Chat Completions format.
     */
    private String extractTextFromOpenRouterResponse(Map response) {
        try {
            if (response.containsKey("choices")) {
                List<Map> choices = (List<Map>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map choice = choices.get(0);
                    if (choice.containsKey("message")) {
                        Map message = (Map) choice.get("message");
                        if (message.containsKey("content")) {
                            return (String) message.get("content");
                        }
                    }
                }
            }
            throw new ApiException("Invalid response structure from OpenRouter API");
        } catch (Exception e) {
            throw new ApiException("Failed to parse OpenRouter API response", e);
        }
    }
    
    /**
     * Parses the structured text response from the AI model.
     * Extracts scores and information from labeled format.
     */
    private ResumeAnalysisResponse parseAnalysisResponse(String textResponse) {
        try {
            logger.info("Parsing AI model response: " + textResponse.substring(0, Math.min(150, textResponse.length())) + "...");
            
            ResumeAnalysisResponse response = new ResumeAnalysisResponse();
            
            // Extract scores
            response.setOverallScore(extractScore(textResponse, "OVERALL SCORE:", 75));
            response.setAtsScore(extractScore(textResponse, "ATS SCORE:", 70));
            
            // Extract skills lists
            response.setTechnicalSkills(extractListSection(textResponse, "TECHNICAL SKILLS:", "MISSING SKILLS:"));
            response.setMissingSkills(extractListSection(textResponse, "MISSING SKILLS:", "PROFESSIONAL SUMMARY:"));
            
            // Extract text sections
            response.setSummary(extractSection(textResponse, "PROFESSIONAL SUMMARY:", "STRENGTHS:"));
            response.setStrengths(extractSection(textResponse, "STRENGTHS:", "IMPROVEMENT SUGGESTIONS:"));
            
            // Extract improvement suggestions
            response.setImprovementSuggestions(extractListSection(textResponse, "IMPROVEMENT SUGGESTIONS:", "SUITABLE JOB ROLES:"));
            
            // Extract job roles
            response.setSuitableJobRoles(extractListSection(textResponse, "SUITABLE JOB ROLES:", "INDUSTRIES:" ));
            
            // If no industries found in response, generate from other data
            List<String> industries = extractListSection(textResponse, "INDUSTRIES:", "");
            if (industries.isEmpty()) {
                industries.add("Software Development");
                industries.add("Technology");
            }
            response.setIndustryMatches(industries);
            
            // Soft skills - generate if not in response
            if (response.getSoftSkills() == null || response.getSoftSkills().isEmpty()) {
                response.setSoftSkills(generateDefaultSoftSkills());
            }
            
            // Build detailed analysis map
            Map<String, Object> detailedAnalysis = new HashMap<>();
            detailedAnalysis.put("overallScore", response.getOverallScore());
            detailedAnalysis.put("atsScore", response.getAtsScore());
            detailedAnalysis.put("technicalSkills", response.getTechnicalSkills());
            detailedAnalysis.put("softSkills", response.getSoftSkills());
            detailedAnalysis.put("missingSkills", response.getMissingSkills());
            detailedAnalysis.put("improvementSuggestions", response.getImprovementSuggestions());
            detailedAnalysis.put("suitableJobRoles", response.getSuitableJobRoles());
            detailedAnalysis.put("industryMatches", response.getIndustryMatches());
            detailedAnalysis.put("summary", response.getSummary());
            detailedAnalysis.put("strengths", response.getStrengths());
            response.setDetailedAnalysis(detailedAnalysis);
            
            return response;
            
        } catch (Exception e) {
            logger.severe("Failed to parse analysis response: " + e.getMessage());
            throw new ApiException("Failed to parse API response", e);
        }
    }
    
    /**
     * Extracts a numerical score from the response text.
     */
    private int extractScore(String text, String label, int defaultValue) {
        try {
            int startIdx = text.indexOf(label);
            if (startIdx == -1) return defaultValue;
            
            startIdx += label.length();
            int endIdx = text.indexOf("\n", startIdx);
            if (endIdx == -1) endIdx = text.indexOf(":", startIdx);
            if (endIdx == -1) endIdx = text.length();
            
            String scoreStr = text.substring(startIdx, endIdx).trim();
            // Extract number from various formats
            String numStr = scoreStr.replaceAll("[^0-9]", "");
            if (numStr.isEmpty()) return defaultValue;
            
            int score = Integer.parseInt(numStr);
            return Math.min(100, Math.max(0, score));
        } catch (Exception e) {
            logger.warning("Could not extract score for " + label + ": " + e.getMessage());
            return defaultValue;
        }
    }
    
    /**
     * Extracts a list section from the response (items starting with "-").
     */
    private List<String> extractListSection(String text, String startLabel, String endLabel) {
        List<String> items = new ArrayList<>();
        try {
            int startIdx = text.indexOf(startLabel);
            if (startIdx == -1) return items;
            
            startIdx += startLabel.length();
            int endIdx = endLabel.isEmpty() ? text.length() : text.indexOf(endLabel, startIdx);
            if (endIdx == -1) endIdx = text.length();
            
            String section = text.substring(startIdx, endIdx);
            String[] lines = section.split("\n");
            
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("-")) {
                    String item = line.substring(1).trim();
                    if (!item.isEmpty()) {
                        items.add(item);
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("Error extracting list section starting with " + startLabel + ": " + e.getMessage());
        }
        return items;
    }
    
    /**
     * Extracts a text section between two labels.
     */
    private String extractSection(String text, String startLabel, String endLabel) {
        try {
            int startIdx = text.indexOf(startLabel);
            if (startIdx == -1) return "";
            
            startIdx += startLabel.length();
            int endIdx = text.indexOf(endLabel, startIdx);
            if (endIdx == -1) endIdx = text.length();
            
            String section = text.substring(startIdx, endIdx).trim();
            return section.isEmpty() ? "" : section;
        } catch (Exception e) {
            logger.warning("Error extracting section from " + startLabel + " to " + endLabel);
            return "";
        }
    }
    
    private List<String> generateDefaultSoftSkills() {
        List<String> softSkills = new ArrayList<>();
        softSkills.add("Problem Solving");
        softSkills.add("Team Collaboration");
        softSkills.add("Communication");
        softSkills.add("Attention to Detail");
        softSkills.add("Adaptability");
        return softSkills;
    }
    
    private ResumeAnalysisResponse createErrorResponse(String errorMessage, long processingTime) {
        ResumeAnalysisResponse errorResponse = new ResumeAnalysisResponse();
        errorResponse.setProcessingTime(processingTime);
        errorResponse.setSuccess(false);
        errorResponse.setErrorMessage(errorMessage);
        
        // Set default empty values
        errorResponse.setTechnicalSkills(new ArrayList<>());
        errorResponse.setSoftSkills(new ArrayList<>());
        errorResponse.setMissingSkills(new ArrayList<>());
        errorResponse.setImprovementSuggestions(new ArrayList<>());
        errorResponse.setSuitableJobRoles(new ArrayList<>());
        errorResponse.setIndustryMatches(new ArrayList<>());
        errorResponse.setSummary("");
        errorResponse.setStrengths("");
        errorResponse.setWeaknesses("");
        
        return errorResponse;
    }
    
    /**
     * Creates a dynamic fallback response based on resume content when API fails.
     * Analyzes resume text for technology keywords and generates relevant analysis.
     */
    private ResumeAnalysisResponse createDynamicFallbackResponse(String resumeText, long processingTime) {
        logger.info("Creating dynamic fallback analysis based on resume content");
        
        // Extract detected technologies from resume
        ResumeKeywords keywords = extractKeywords(resumeText);
        
        ResumeAnalysisResponse fallback = new ResumeAnalysisResponse();
        fallback.setProcessingTime(processingTime);
        fallback.setSuccess(true);
        fallback.setErrorMessage(null);
        
        // Generate scores based on keyword count (0-100)
        int keywordCount = keywords.getTotalCount();
        int overallScore = Math.min(95, 55 + (keywordCount * 3)); // Base 55, +3 per keyword, max 95
        int atsScore = Math.min(92, 50 + (keywordCount * 2)); // Base 50, +2 per keyword, max 92
        fallback.setOverallScore(overallScore);
        fallback.setAtsScore(atsScore);
        
        // Build dynamic technical skills from detected keywords
        fallback.setTechnicalSkills(buildDynamicTechnicalSkills(keywords));
        fallback.setSoftSkills(generateDefaultSoftSkills());
        fallback.setMissingSkills(buildDynamicMissingSkills(keywords));
        fallback.setImprovementSuggestions(buildDynamicSuggestions(keywords));
        fallback.setSuitableJobRoles(buildDynamicJobRoles(keywords));
        fallback.setIndustryMatches(buildDynamicIndustries(keywords));
        
        // Generate context-aware text summaries
        fallback.setSummary(buildDynamicSummary(keywords));
        fallback.setStrengths(buildDynamicStrengths(keywords));
        fallback.setWeaknesses(buildDynamicWeaknesses(keywords));
        
        // Build detailed analysis map
        Map<String, Object> detailedAnalysis = new HashMap<>();
        detailedAnalysis.put("overallScore", overallScore);
        detailedAnalysis.put("atsScore", atsScore);
        detailedAnalysis.put("technicalSkills", fallback.getTechnicalSkills());
        detailedAnalysis.put("softSkills", fallback.getSoftSkills());
        detailedAnalysis.put("missingSkills", fallback.getMissingSkills());
        detailedAnalysis.put("improvementSuggestions", fallback.getImprovementSuggestions());
        detailedAnalysis.put("suitableJobRoles", fallback.getSuitableJobRoles());
        detailedAnalysis.put("industryMatches", fallback.getIndustryMatches());
        detailedAnalysis.put("summary", fallback.getSummary());
        detailedAnalysis.put("strengths", fallback.getStrengths());
        detailedAnalysis.put("weaknesses", fallback.getWeaknesses());
        fallback.setDetailedAnalysis(detailedAnalysis);
        
        return fallback;
    }
    
    /**
     * Keyword detection inner class - tracks detected technologies in resume
     */
    private ResumeKeywords extractKeywords(String resumeText) {
        String text = resumeText.toLowerCase();
        ResumeKeywords keywords = new ResumeKeywords();
        
        // Check programming languages
        if (text.contains("java")) keywords.hasJava = true;
        if (text.contains("python")) keywords.hasPython = true;
        if (text.contains("javascript") || text.contains("js")) keywords.hasJavaScript = true;
        if (text.contains("c#") || text.contains("csharp")) keywords.hasCSharp = true;
        if (text.contains("go") || text.contains("golang")) keywords.hasGo = true;
        if (text.contains("rust")) keywords.hasRust = true;
        if (text.contains("kotlin")) keywords.hasKotlin = true;
        if (text.contains("typescript") || text.contains("ts")) keywords.hasTypeScript = true;
        
        // Check frameworks
        if (text.contains("spring")) keywords.hasSpring = true;
        if (text.contains("react")) keywords.hasReact = true;
        if (text.contains("angular")) keywords.hasAngular = true;
        if (text.contains("django")) keywords.hasDjango = true;
        if (text.contains("fastapi") || text.contains("flask")) keywords.hasFlask = true;
        if (text.contains("nodejs") || text.contains("node.js")) keywords.hasNodeJs = true;
        if (text.contains("express")) keywords.hasExpress = true;
        if (text.contains("vue")) keywords.hasVue = true;
        
        // Check databases
        if (text.contains("sql") || text.contains("mysql") || text.contains("postgresql")) keywords.hasSQL = true;
        if (text.contains("mongodb") || text.contains("nosql")) keywords.hasNoSQL = true;
        if (text.contains("redis")) keywords.hasRedis = true;
        if (text.contains("elasticsearch")) keywords.hasElasticsearch = true;
        
        // Check DevOps/Cloud
        if (text.contains("docker")) keywords.hasDocker = true;
        if (text.contains("kubernetes") || text.contains("k8s")) keywords.hasKubernetes = true;
        if (text.contains("aws")) keywords.hasAWS = true;
        if (text.contains("azure")) keywords.hasAzure = true;
        if (text.contains("gcp") || text.contains("google cloud")) keywords.hasGCP = true;
        if (text.contains("terraform")) keywords.hasTerraform = true;
        if (text.contains("jenkins") || text.contains("gitlab") || text.contains("ci/cd")) keywords.hasCI_CD = true;
        
        // Check tools/platforms
        if (text.contains("git") || text.contains("github") || text.contains("gitlab")) keywords.hasGit = true;
        if (text.contains("jira")) keywords.hasJira = true;
        if (text.contains("agile") || text.contains("scrum")) keywords.hasAgile = true;
        if (text.contains("api") || text.contains("rest")) keywords.hasAPI = true;
        if (text.contains("microservices")) keywords.hasMicroservices = true;
        
        return keywords;
    }
    
    private List<String> buildDynamicTechnicalSkills(ResumeKeywords kw) {
        List<String> skills = new ArrayList<>();
        
        if (kw.hasJava) skills.add("Java");
        if (kw.hasPython) skills.add("Python");
        if (kw.hasJavaScript) skills.add("JavaScript");
        if (kw.hasTypeScript) skills.add("TypeScript");
        if (kw.hasKotlin) skills.add("Kotlin");
        if (kw.hasCSharp) skills.add("C#");
        if (kw.hasGo) skills.add("Go");
        if (kw.hasRust) skills.add("Rust");
        
        if (kw.hasSpring) skills.add("Spring Framework");
        if (kw.hasReact) skills.add("React");
        if (kw.hasAngular) skills.add("Angular");
        if (kw.hasDjango) skills.add("Django");
        if (kw.hasFlask) skills.add("Flask");
        if (kw.hasNodeJs) skills.add("Node.js");
        if (kw.hasExpress) skills.add("Express.js");
        if (kw.hasVue) skills.add("Vue.js");
        
        if (kw.hasSQL) skills.add("SQL & Relational Databases");
        if (kw.hasNoSQL) skills.add("NoSQL Databases");
        if (kw.hasRedis) skills.add("Redis");
        if (kw.hasElasticsearch) skills.add("Elasticsearch");
        
        if (kw.hasDocker) skills.add("Docker");
        if (kw.hasKubernetes) skills.add("Kubernetes");
        if (kw.hasAWS) skills.add("Amazon Web Services (AWS)");
        if (kw.hasAzure) skills.add("Microsoft Azure");
        if (kw.hasGCP) skills.add("Google Cloud Platform");
        if (kw.hasTerraform) skills.add("Terraform");
        
        if (kw.hasCI_CD) skills.add("CI/CD Pipelines");
        if (kw.hasGit) skills.add("Git & Version Control");
        if (kw.hasAPI) skills.add("RESTful API Development");
        if (kw.hasMicroservices) skills.add("Microservices Architecture");
        if (kw.hasAgile) skills.add("Agile/Scrum Methodologies");
        
        // Ensure minimum skills list
        if (skills.isEmpty()) {
            skills.add("Software Development");
            skills.add("Problem Solving");
        }
        
        return skills;
    }
    
    private List<String> buildDynamicMissingSkills(ResumeKeywords kw) {
        List<String> missing = new ArrayList<>();
        
        if (!kw.hasDocker && !kw.hasKubernetes) {
            missing.add("Container Technologies (Docker/Kubernetes)");
        }
        if (!kw.hasAWS && !kw.hasAzure && !kw.hasGCP) {
            missing.add("Cloud Platform Experience");
        }
        if (!kw.hasCI_CD) {
            missing.add("CI/CD Pipeline Implementation");
        }
        if (!kw.hasMicroservices) {
            missing.add("Microservices Architecture");
        }
        if (!kw.hasAPI && !kw.hasNodeJs) {
            missing.add("API Design & Documentation");
        }
        
        if (kw.hasJava && !kw.hasSpring) {
            missing.add("Modern Java Frameworks (Spring Boot)");
        }
        if (kw.hasJavaScript && !kw.hasTypeScript) {
            missing.add("TypeScript for Type Safety");
        }
        if (kw.hasPython && !kw.hasDjango && !kw.hasFlask) {
            missing.add("Web Framework Experience");
        }
        
        while (missing.size() < 5) {
            if (!kw.hasTerraform) missing.add("Infrastructure as Code");
            else if (!kw.hasJira) missing.add("Project Management Tools");
            else if (!kw.hasAgile) missing.add("Agile Project Management");
            else missing.add("Technical Documentation");
        }
        
        return missing.subList(0, Math.min(5, missing.size()));
    }
    
    private List<String> buildDynamicSuggestions(ResumeKeywords kw) {
        List<String> suggestions = new ArrayList<>();
        
        suggestions.add("Add quantified achievements and metrics to demonstrate measurable impact");
        suggestions.add("Include specific projects that showcase the technologies you listed");
        
        if (!kw.hasDocker && !kw.hasKubernetes) {
            suggestions.add("Gain experience with containerization (Docker) and orchestration (Kubernetes)");
        }
        if (!kw.hasCI_CD) {
            suggestions.add("Learn and implement CI/CD pipelines for automated testing and deployment");
        }
        if (!kw.hasCloudExperience()) {
            suggestions.add("Obtain cloud platform certification (AWS, Azure, or GCP)");
        }
        if (kw.hasJava && !kw.hasSpring) {
            suggestions.add("Master Spring Boot framework for enterprise Java development");
        }
        if (kw.hasPython && !kw.hasDjango && !kw.hasFlask) {
            suggestions.add("Learn web frameworks (Django or FastAPI) to expand Python expertise");
        }
        if (!kw.hasAPI) {
            suggestions.add("Document API design patterns and best practices in your portfolio");
        }
        
        while (suggestions.size() < 5) {
            suggestions.add("Contribute to open-source projects to gain diverse technical experience");
        }
        
        return suggestions.subList(0, Math.min(7, suggestions.size()));
    }
    
    private List<String> buildDynamicJobRoles(ResumeKeywords kw) {
        List<String> roles = new ArrayList<>();
        
        if (kw.hasJava || kw.hasPython) {
            roles.add("Senior Backend Developer");
            roles.add("Backend Engineer");
        }
        if (kw.hasSpring) {
            roles.add("Spring Boot Developer");
        }
        if (kw.hasJavaScript || kw.hasTypeScript) {
            roles.add("Full Stack Developer");
        }
        if (kw.hasReact || kw.hasAngular || kw.hasVue) {
            roles.add("Frontend Engineer");
        }
        if (kw.hasDocker || kw.hasKubernetes) {
            roles.add("DevOps Engineer");
        }
        if (kw.hasAWS || kw.hasAzure || kw.hasGCP) {
            roles.add("Cloud Solutions Architect");
        }
        if (kw.hasNodeJs) {
            roles.add("Node.js Developer");
        }
        if (kw.hasMicroservices) {
            roles.add("Enterprise Architect");
        }
        
        while (roles.size() < 5) {
            roles.add("Software Engineer");
        }
        
        return roles.subList(0, Math.min(5, roles.size()));
    }
    
    private List<String> buildDynamicIndustries(ResumeKeywords kw) {
        List<String> industries = new ArrayList<>();
        
        if (kw.hasJava || kw.hasAPI) {
            industries.add("Enterprise Software & Solutions");
        }
        if (kw.hasReact || kw.hasAngular) {
            industries.add("E-commerce & Retail");
        }
        if (kw.hasAWS || kw.hasAzure || kw.hasMicroservices) {
            industries.add("Cloud Computing & SaaS");
        }
        if (kw.hasPython) {
            industries.add("Data Science & AI/ML");
        }
        if (kw.hasDocker || kw.hasKubernetes) {
            industries.add("DevOps & Infrastructure");
        }
        if (kw.hasNodeJs) {
            industries.add("Real-time Applications");
        }
        industries.add("Financial Technology (FinTech)");
        industries.add("Healthcare Technology");
        
        return industries.subList(0, Math.min(5, industries.size()));
    }
    
    private String buildDynamicSummary(ResumeKeywords kw) {
        StringBuilder summary = new StringBuilder();
        
        if (kw.hasJava && kw.hasSpring) {
            summary.append("Skilled Spring Boot developer with expertise in building enterprise-grade backend systems");
        } else if (kw.hasPython) {
            summary.append("Experienced Python developer with strong capabilities in backend development");
        } else if (kw.hasJavaScript || kw.hasTypeScript) {
            summary.append("Fullstack JavaScript developer skilled in modern web technologies");
        } else {
            summary.append("Proficient software developer with solid technical foundation");
        }
        
        if (kw.hasDocker || kw.hasKubernetes) {
            summary.append(" and containerization expertise");
        }
        if (kw.hasAWS || kw.hasAzure || kw.hasGCP) {
            summary.append(", with cloud platform experience");
        }
        
        summary.append(". Demonstrates strong problem-solving abilities and commitment to code quality. ");
        summary.append("Proven ability to deliver scalable solutions and collaborate effectively with cross-functional teams.");
        
        return summary.toString();
    }
    
    private String buildDynamicStrengths(ResumeKeywords kw) {
        StringBuilder strengths = new StringBuilder();
        
        strengths.append("Solid technical foundation");
        
        if (kw.hasAPI) {
            strengths.append(" with proven expertise in API design and RESTful services");
        }
        if (kw.hasSQL) {
            strengths.append(". Strong understanding of relational databases and data modeling");
        }
        if (kw.hasGit) {
            strengths.append(". Proficient with version control and collaborative development practices");
        }
        
        strengths.append(". ");
        
        if (kw.hasDocker || kw.hasKubernetes) {
            strengths.append("Experience with containerization and orchestration platforms enables deployment of robust solutions. ");
        }
        if (kw.hasCI_CD) {
            strengths.append("Knowledge of CI/CD pipelines ensures automated quality assurance. ");
        }
        
        strengths.append("Excellent track record of delivering high-quality code with attention to best practices. ");
        strengths.append("Strong communicator with ability to work effectively in team environments and mentor junior developers.");
        
        return strengths.toString();
    }
    
    private String buildDynamicWeaknesses(ResumeKeywords kw) {
        StringBuilder weaknesses = new StringBuilder();
        
        List<String> gaps = new ArrayList<>();
        
        if (!kw.hasDocker && !kw.hasKubernetes) {
            gaps.add("containerization and orchestration platforms (Docker, Kubernetes)");
        }
        if (!kw.hasCloudExperience()) {
            gaps.add("formal cloud infrastructure experience");
        }
        if (!kw.hasMicroservices) {
            gaps.add("distributed microservices architecture");
        }
        if (!kw.hasCI_CD) {
            gaps.add("automated CI/CD pipeline implementation");
        }
        
        if (gaps.isEmpty()) {
            weaknesses.append("Areas for growth include exploring emerging technologies and cloud-native patterns. ");
        } else {
            weaknesses.append("Limited exposure to ").append(String.join(", ", gaps)).append(". ");
        }
        
        weaknesses.append("Would benefit from expanding knowledge in modern DevOps practices and cloud-native development patterns. ");
        weaknesses.append("Consider gaining hands-on experience with additional frameworks and architectural patterns to enhance capabilities in rapidly evolving tech landscape.");
        
        return weaknesses.toString();
    }
    
    /**
     * Inner class to track detected keywords from resume
     */
    private static class ResumeKeywords {
        boolean hasJava, hasPython, hasJavaScript, hasTypeScript, hasCSharp, hasGo, hasRust, hasKotlin;
        boolean hasSpring, hasReact, hasAngular, hasDjango, hasFlask, hasNodeJs, hasExpress, hasVue;
        boolean hasSQL, hasNoSQL, hasRedis, hasElasticsearch;
        boolean hasDocker, hasKubernetes, hasAWS, hasAzure, hasGCP, hasTerraform, hasCI_CD;
        boolean hasGit, hasJira, hasAgile, hasAPI, hasMicroservices;
        
        int getTotalCount() {
            int count = 0;
            count += (hasJava ? 1 : 0) + (hasPython ? 1 : 0) + (hasJavaScript ? 1 : 0) + (hasTypeScript ? 1 : 0);
            count += (hasSpring ? 1 : 0) + (hasReact ? 1 : 0) + (hasAngular ? 1 : 0) + (hasDjango ? 1 : 0);
            count += (hasSQL ? 1 : 0) + (hasNoSQL ? 1 : 0) + (hasDocker ? 1 : 0) + (hasKubernetes ? 1 : 0);
            count += (hasAWS ? 1 : 0) + (hasAzure ? 1 : 0) + (hasGCP ? 1 : 0) + (hasCI_CD ? 1 : 0);
            count += (hasGit ? 1 : 0) + (hasAPI ? 1 : 0) + (hasMicroservices ? 1 : 0);
            return count;
        }
        
        boolean hasCloudExperience() {
            return hasAWS || hasAzure || hasGCP;
        }
    }
}
