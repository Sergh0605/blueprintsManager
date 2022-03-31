package com.dataart.blueprintsmanager.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DocumentDto {
    private Long id;
    private BasicDto project;
    private Integer numberInProject;
    @NotNull
    @Valid
    private DocumentTypeDto documentType;
    @NotEmpty
    private String name;
    @NotEmpty
    private String code;
    @Valid
    private BasicDto designer;
    @Valid
    private BasicDto supervisor;
    private Boolean reassemblyRequired;
    private LocalDateTime editTime;
    private Boolean deleted;
    private String documentFullCode;
    @NotNull
    private Long version;
}
