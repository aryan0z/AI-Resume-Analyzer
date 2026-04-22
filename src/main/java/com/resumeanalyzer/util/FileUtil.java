package com.resumeanalyzer.util;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Utility class for file operations
 */
public class FileUtil {
    
    /**
     * Convert MultipartFile to bytes
     */
    public static byte[] convertMultipartFileToBytes(MultipartFile file) throws IOException {
        if (file == null) {
            return new byte[0];
        }
        return file.getBytes();
    }
    
    /**
     * Get file extension
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
    
    /**
     * Format file size in readable format
     */
    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        
        return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
    
    /**
     * Sanitize filename
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null) {
            return "";
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
