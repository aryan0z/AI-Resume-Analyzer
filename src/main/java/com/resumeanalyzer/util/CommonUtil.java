package com.resumeanalyzer.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for common operations
 */
public class CommonUtil {
    
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Get current timestamp
     */
    public static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DATE_FORMATTER);
    }
    
    /**
     * Validate email
     */
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
    
    /**
     * Check if string is empty or null
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Parse integer safely
     */
    public static int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Calculate percentage
     */
    public static double calculatePercentage(double value, double total) {
        if (total == 0) return 0;
        return (value / total) * 100;
    }
}
