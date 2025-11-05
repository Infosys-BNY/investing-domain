package com.bny.shared.util.transformation;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DateTimeFormatter {
    
    private static final java.time.format.DateTimeFormatter ISO_FORMATTER = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final java.time.format.DateTimeFormatter DISPLAY_FORMATTER = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public String formatIso(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(ISO_FORMATTER);
    }
    
    public String formatDisplay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DISPLAY_FORMATTER);
    }
    
    public LocalDateTime parseIso(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeString, ISO_FORMATTER);
    }
}
