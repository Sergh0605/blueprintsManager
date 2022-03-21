package com.dataart.blueprintsmanager.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExceptionDto {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String path;
}
