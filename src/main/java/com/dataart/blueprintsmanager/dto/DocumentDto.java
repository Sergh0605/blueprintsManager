package com.dataart.blueprintsmanager.dto;

import com.dataart.blueprintsmanager.persistence.entity.DocumentEntity;
import com.dataart.blueprintsmanager.persistence.entity.DocumentType;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DocumentDto {
    static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private Long id;
    private Long projectId;
    private Integer numberInProject;
    private Long typeId;
    private String name;
    private String code;
    private Long designerId;
    private Long supervisorId;
    private Boolean reassemblyRequired;
    private LocalDateTime editTime;
    private byte[] documentInPdf;
    private String documentFileName;
    private String documentFullCode;

    public String getEditTimeWithFormat() {
        return getEditTime().format(dateTimeFormatter).toUpperCase(Locale.ROOT);
    }

    public DocumentDto(DocumentEntity document) {
        if (document != null) {
            this.id = document.getId();
            this.projectId = Optional.ofNullable(document.getProject()).map(ProjectEntity::getId).orElse(null);
            this.numberInProject = document.getNumberInProject();
            this.typeId = Optional.ofNullable(document.getDocumentType()).map(DocumentType::getId).orElse(null);
            this.name = document.getName();
            this.code = document.getCode();
            this.designerId = Optional.ofNullable(document.getDesigner()).map(UserEntity::getId).orElse(null);
            this.supervisorId = Optional.ofNullable(document.getSupervisor()).map(UserEntity::getId).orElse(null);
            this.reassemblyRequired = document.getReassemblyRequired();
            this.editTime = document.getEditTime();
        }
    }

    public DocumentEntity updateEntity(DocumentEntity entity) {
        return DocumentEntity.builder()
                .id(Optional.ofNullable(this.id).orElse(entity.getId()))
                .name(Optional.ofNullable(this.name).orElse(entity.getName()))
                .project(Optional.ofNullable(this.projectId).map(x -> ProjectEntity.builder().id(x).build()).orElse(entity.getProject()))
                .numberInProject(Optional.ofNullable(this.numberInProject).orElse(entity.getNumberInProject()))
                .documentType(Optional.ofNullable(this.typeId).map(DocumentType::getById).orElse(entity.getDocumentType()))
                .code(Optional.ofNullable(this.code).orElse(entity.getCode()))
                .designer(Optional.ofNullable(this.designerId).map(x -> UserEntity.builder().id(x).build()).orElse(entity.getDesigner()))
                .supervisor(Optional.ofNullable(this.supervisorId).map(x -> UserEntity.builder().id(x).build()).orElse(entity.getSupervisor()))
                .editTime(LocalDateTime.now())
                .reassemblyRequired(true)
                .build();
    }
}
