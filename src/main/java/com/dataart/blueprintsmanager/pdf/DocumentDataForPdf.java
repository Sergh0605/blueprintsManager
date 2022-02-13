package com.dataart.blueprintsmanager.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DocumentDataForPdf {
    private String documentCode;
    private String documentName;
    private String designerName;
    private byte[] designerSign;
    private String supervisorName;
    private byte[] supervisorSign;
    private String controllerName;
    private byte[] controllerSign;
    private String chiefEngineerName;
    private byte[] chiefEngineerSign;
    private String stage;
    private String projectName;
    private String objectAddress;
    private String releaseDate;
    private CompanyDataForPdf company;
    private Long volumeNumber;
    private String volumeName;
    private String releaseDateForCover;
    private String codeForCover;
    private String stageForCover;
}