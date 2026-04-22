package com.resumeanalyzer.service;

import com.resumeanalyzer.exception.FileProcessingException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

@Service
public class PdfExtractionService {
    
    private static final Logger logger = Logger.getLogger(PdfExtractionService.class.getName());
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_PAGES = 50;
    
    public String extractTextFromPdf(MultipartFile file) {
        logger.info("Starting PDF text extraction for file: " + file.getOriginalFilename());
        
        validateFile(file);
        
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {
            
            validatePages(document);
            
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            
            logger.info("Successfully extracted " + text.length() + " characters from PDF");
            return text;
            
        } catch (IOException e) {
            logger.severe("Error extracting PDF text: " + e.getMessage());
            throw new FileProcessingException("Failed to extract text from PDF", e);
        }
    }
    
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileProcessingException("File is empty or null");
        }
        
        if (!isPdfFile(file)) {
            throw new FileProcessingException("File must be a PDF file");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileProcessingException("File size exceeds 10MB limit");
        }
    }
    
    private void validatePages(PDDocument document) {
        if (document.getNumberOfPages() > MAX_PAGES) {
            throw new FileProcessingException("PDF exceeds maximum page limit of " + MAX_PAGES);
        }
    }
    
    private boolean isPdfFile(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        return (contentType != null && contentType.equals("application/pdf")) ||
               (filename != null && filename.toLowerCase().endsWith(".pdf"));
    }
    
}
