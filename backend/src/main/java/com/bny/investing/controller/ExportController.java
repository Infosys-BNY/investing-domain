package com.bny.investing.controller;

import com.bny.investing.dto.ExportRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/export")
@Validated
@RequiredArgsConstructor
public class ExportController {
    
    @PostMapping("/holdings")
    public ResponseEntity<byte[]> exportHoldings(
            @RequestBody ExportRequest request) {
        
        byte[] emptyFile = new byte[0];
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "holdings-export.xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(emptyFile);
    }
}
