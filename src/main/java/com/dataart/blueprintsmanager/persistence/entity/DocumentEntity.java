package com.dataart.blueprintsmanager.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DocumentEntity {
    private Long id;
    private ProjectEntity project;
    private DocumentType documentType;
    private String name;
    private String code;
    private UserEntity designer;
    private UserEntity supervisor;
    private byte[] contentInPdf;
    private Boolean reassemblyRequired;
    private byte[] documentInPdf;

}
