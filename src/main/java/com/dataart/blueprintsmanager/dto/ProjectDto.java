package com.dataart.blueprintsmanager.dto;

import com.dataart.blueprintsmanager.persistence.entity.CompanyEntity;
import com.dataart.blueprintsmanager.persistence.entity.ProjectEntity;
import com.dataart.blueprintsmanager.persistence.entity.StageEntity;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProjectDto {
    static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private Long id;
    private String name;
    private String objectName;
    private String objectAddress;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate releaseDate;

    private Long volumeNumber;
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
    private Long companyId;
    private Long designerId;
    private Long supervisorId;
    private Long chiefId;
    private Long controllerId;
    private Long stageId;

    public ProjectDto(ProjectEntity project) {
        if (project != null) {
            this.id = project.getId();
            this.name = project.getName();
            this.objectName = project.getObjectName();
            this.objectAddress = project.getObjectAddress();
            this.releaseDate = project.getReleaseDate();
            this.volumeNumber = project.getVolumeNumber();
            this.volumeName = project.getVolumeName();
            this.code = project.getCode();
            this.designerName = Optional.ofNullable(project.getDesigner()).map(UserEntity::getLastName).orElse("");
            this.supervisorName = Optional.ofNullable(project.getSupervisor()).map(UserEntity::getLastName).orElse("");
            this.chiefName = Optional.ofNullable(project.getChief()).map(UserEntity::getLastName).orElse("");
            this.controllerName = Optional.ofNullable(project.getController()).map(UserEntity::getLastName).orElse("");
            this.companyName = Optional.ofNullable(project.getCompany()).map(CompanyEntity::getName).orElse("");
            this.stageCode = Optional.ofNullable(project.getStage()).map(StageEntity::getCode).orElse("");
            this.designerId = Optional.ofNullable(project.getDesigner()).map(UserEntity::getId).orElse(0L);
            this.supervisorId = Optional.ofNullable(project.getSupervisor()).map(UserEntity::getId).orElse(0L);
            this.chiefId = Optional.ofNullable(project.getChief()).map(UserEntity::getId).orElse(0L);
            this.controllerId = Optional.ofNullable(project.getController()).map(UserEntity::getId).orElse(0L);
            this.companyId = Optional.ofNullable(project.getCompany()).map(CompanyEntity::getId).orElse(0L);
            this.stageId = Optional.ofNullable(project.getStage()).map(StageEntity::getId).orElse(0L);
            this.reassemblyRequired = project.getReassemblyRequired();
            this.editTime = project.getEditTime();
        }
    }

    public String getEditTimeWithFormat() {
        return getEditTime().format(dateTimeFormatter).toUpperCase(Locale.ROOT);
    }

    public static ProjectDto getEmpty(){
        return ProjectDto.builder()
                .id(0L)
                .name("")
                .objectName("")
                .objectAddress("")
                .releaseDate(LocalDate.now())
                .volumeNumber(1L)
                .volumeName("")
                .code("NEW")
                .designerName("")
                .supervisorName("")
                .chiefName("")
                .controllerName("")
                .companyName("")
                .stageCode("")
                .reassemblyRequired(false)
                .editTime(LocalDateTime.now()).build();


    }
}
