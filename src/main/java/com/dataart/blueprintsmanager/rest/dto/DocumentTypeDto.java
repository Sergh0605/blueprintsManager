package com.dataart.blueprintsmanager.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DocumentTypeDto {
    @NotNull
    private Long id;
    private String name;
    private String type;
    private Boolean unmodified;
}
