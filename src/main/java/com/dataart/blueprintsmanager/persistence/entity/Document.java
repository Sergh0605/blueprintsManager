package com.dataart.blueprintsmanager.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Document {
    private Long id;
    private Project project;
    private DocumentType documentType;
    private String name;
    private String code;
    private User designer;
    private User supervisor;
    private byte[] contentInPdf;
    private Boolean reassemblyRequired;
    private byte[] documentInPdf;

}
