package com.bny.shared.util.pagination;

import com.bny.shared.dto.response.PaginatedResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaginationHelper {
    
    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 1000;
    
    public int validatePageSize(int pageSize) {
        if (pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
    
    public int validatePageNumber(int pageNumber) {
        return Math.max(0, pageNumber);
    }
    
    public int calculateOffset(int pageNumber, int pageSize) {
        return pageNumber * pageSize;
    }
    
    public <T> PaginatedResponse<T> createResponse(List<T> content, int pageNumber, int pageSize, long totalElements) {
        return PaginatedResponse.of(content, pageNumber, pageSize, totalElements);
    }
}
