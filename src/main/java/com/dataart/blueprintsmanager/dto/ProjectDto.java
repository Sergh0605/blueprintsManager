package com.dataart.blueprintsmanager.dto;

import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProjectDto {
    private Long id;
    private String name;
    private String objectName;
    private String objectAddress;
    private LocalDate releaseDate;
    private Integer volumeNumber;
    private String volumeName;
    private String code;
    private String designerName;
    private String supervisorName;
    private String chiefName;
    private String controllerName;
    private String companyName;
    private String stageCode;
    private Boolean reassemblyRequired;
    private LocalDateTime editTime;

    public ProjectDto(ProjectEntity project) {
        this.id = project.getId();
        this.name = project.getName();
        this.objectName = project.getObjectName();
        this.objectAddress = project.getObjectAddress();
        this.releaseDate = project.getReleaseDate();
        this.volumeNumber = project.getVolumeNumber();
        this.volumeName = project.getVolumeName();
        this.code = project.getCode();
        if (project.getDesigner() != null) {
            this.designerName = project.getDesigner().getLastName();
        } else this.designerName = "";
        if (project.getSupervisor() != null) {
            this.supervisorName = project.getSupervisor().getLastName();
        } else this.supervisorName = "";
        if (project.getChief() != null) {
            this.chiefName = project.getChief().getLastName();
        } else this.chiefName = "";
        if (project.getController() != null) {
            this.controllerName = project.getController().getLastName();
        } else this.controllerName = "";
        if (project.getCompany() != null) {
            this.companyName = project.getCompany().getName();
        } else this.companyName = "";
        if (project.getStage() != null) {
            this.stageCode = project.getStage().getCode();
        } else this.stageCode = "";
        this.reassemblyRequired = project.getReassemblyRequired();
        this.editTime = project.getEditTime();
    }

    public String getEditTimeWithFormat() {
        return getEditTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")).toUpperCase(Locale.ROOT);
    }
}
