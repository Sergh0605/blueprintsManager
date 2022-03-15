package com.dataart.blueprintsmanager.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProjectFileDto {
    @NotNull
    private Long id;
    private LocalDateTime creationTime;
}
