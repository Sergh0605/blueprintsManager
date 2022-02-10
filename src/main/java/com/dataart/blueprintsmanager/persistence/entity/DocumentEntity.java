package com.dataart.blueprintsmanager.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DocumentEntity {
    private Long id;
    private ProjectEntity project;
    private Integer numberInProject;
    private DocumentType documentType;
    private String name;
    private String code;
    private UserEntity designer;
    private UserEntity supervisor;
    private byte[] contentInPdf;
    private Boolean reassemblyRequired;
    private byte[] documentInPdf;
    private LocalDateTime editTime;

}
