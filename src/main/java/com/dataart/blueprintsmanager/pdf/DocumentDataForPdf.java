package com.dataart.blueprintsmanager.pdf;

import com.dataart.blueprintsmanager.persistence.entity.DocumentEntity;
import com.dataart.blueprintsmanager.persistence.entity.FileEntity;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DocumentDataForPdf {
    private static final DateTimeFormatter releaseDateFormat = DateTimeFormatter.ofPattern("MM.yy");
    private static final DateTimeFormatter releaseDateForCoverFormat = DateTimeFormatter.ofPattern("yyyy");
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

    static public DocumentDataForPdf getDocumentDataForPdf(DocumentEntity document, String fullCode) {
        CompanyDataForPdf companyDataForPdf = CompanyDataForPdf.builder()
                .city(document.getProject().getCompany().getCity())
                .logo(Optional.ofNullable(document.getProject().getCompany().getLogoFile())
                        .map(FileEntity::getFileInBytes)
                        .orElse(null))
                .signerName(document.getProject().getCompany().getSignerName())
                .signerPosition(document.getProject().getCompany().getSignerPosition())
                .name(document.getProject().getCompany().getName())
                .build();
        return DocumentDataForPdf.builder()
                .documentCode(fullCode)
                .documentName(document.getName())
                .designerName(Optional.ofNullable(document.getDesigner())
                        .map(UserEntity::getLastName)
                        .orElse(Optional.ofNullable(document.getProject().getDesigner())
                                .map(UserEntity::getLastName)
                                .orElse("")))
                .designerSign(Optional.ofNullable(document.getDesigner()).flatMap(u -> Optional.ofNullable(u.getSignatureFile())
                                .map(FileEntity::getFileInBytes))
                        .orElse(Optional.ofNullable(document.getProject().getDesigner()).flatMap(u -> Optional.ofNullable(u.getSignatureFile())
                                        .map(FileEntity::getFileInBytes))
                                .orElse(null)))
                .supervisorName(Optional.ofNullable(document.getSupervisor())
                        .map(UserEntity::getLastName)
                        .orElse(Optional.ofNullable(document.getProject().getSupervisor())
                                .map(UserEntity::getLastName)
                                .orElse("")))
                .supervisorSign(Optional.ofNullable(document.getSupervisor()).flatMap(u -> Optional.ofNullable(u.getSignatureFile())
                                .map(FileEntity::getFileInBytes))
                        .orElse(Optional.ofNullable(document.getProject().getSupervisor()).flatMap(u -> Optional.ofNullable(u.getSignatureFile())
                                        .map(FileEntity::getFileInBytes))
                                .orElse(null)))
                .controllerName(Optional.ofNullable(document.getProject().getController())
                        .map(UserEntity::getLastName)
                        .orElse(null))
                .controllerSign(Optional.ofNullable(document.getProject().getController()).flatMap(u -> Optional.ofNullable(u.getSignatureFile())
                                .map(FileEntity::getFileInBytes))
                        .orElse(null))
                .chiefEngineerName(Optional.ofNullable(document.getProject().getChief())
                        .map(UserEntity::getLastName)
                        .orElse(null))
                .chiefEngineerSign(Optional.ofNullable(document.getProject().getChief()).flatMap(u -> Optional.ofNullable(u.getSignatureFile())
                                .map(FileEntity::getFileInBytes))
                        .orElse(null))
                .stage(document.getProject().getStage().getCode())
                .projectName(document.getProject().getName())
                .objectAddress(document.getProject().getObjectAddress())
                .releaseDate(document.getProject().getReleaseDate().format(releaseDateFormat))
                .company(companyDataForPdf)
                .volumeNumber(document.getProject().getVolumeNumber())
                .volumeName(document.getProject().getVolumeName())
                .releaseDateForCover(document.getProject().getReleaseDate().format(releaseDateForCoverFormat))
                .codeForCover(document.getProject().getCode())
                .stageForCover(document.getProject().getStage().getName())
                .build();
    }
}