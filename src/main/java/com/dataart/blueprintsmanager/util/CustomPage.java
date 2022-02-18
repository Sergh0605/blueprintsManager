package com.dataart.blueprintsmanager.util;

import lombok.Data;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Data
public class CustomPage<T> {
    private final int total;
    private List<T> content;
    private Pageable pageable;

    public CustomPage(List<T> content, Pageable pageable, int total) {
        this.total = total;
        this.content = content;
        this.pageable = pageable;
    }

    public int getTotalPages(){
        int totalPages = total / pageable.getPageSize();
        if (total % pageable.getPageSize() > 0) {
            totalPages++;
        }
        return totalPages;
    }

}
