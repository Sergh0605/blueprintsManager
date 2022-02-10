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

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    private Long volumeNumber;
    private String volumeName;
    private String code;
    private String designerName;
    private String supervisorName;
    private String chiefName;
    private String controllerName;
    private String companyName;
    private Boolean reassemblyRequired;
    private LocalDateTime editTime;
    private Long companyId;
    private Long designerId;
    private Long supervisorId;
    private Long chiefId;
    private Long controllerId;
    private Long stageId;
    private String stageName;

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
            this.companyName = Optional.ofNullable(project.getCompany()).map(CompanyEntity::getName).orElse("");
            this.stageName = Optional.ofNullable(project.getStage()).map(StageEntity::getName).orElse("");
            this.designerId = Optional.ofNullable(project.getDesigner()).map(UserEntity::getId).orElse(null);
            this.supervisorId = Optional.ofNullable(project.getSupervisor()).map(UserEntity::getId).orElse(null);
            this.chiefId = Optional.ofNullable(project.getChief()).map(UserEntity::getId).orElse(null);
            this.controllerId = Optional.ofNullable(project.getController()).map(UserEntity::getId).orElse(null);
            this.companyId = Optional.ofNullable(project.getCompany()).map(CompanyEntity::getId).orElse(null);
            this.stageId = Optional.ofNullable(project.getStage()).map(StageEntity::getId).orElse(null);
            this.reassemblyRequired = project.getReassemblyRequired();
            this.editTime = project.getEditTime();
        }
    }

    public String getEditTimeWithFormat() {
        return getEditTime().format(dateTimeFormatter).toUpperCase(Locale.ROOT);
    }

    public ProjectEntity updateEntity(ProjectEntity entity) {
        return ProjectEntity.builder()
                .id(Optional.ofNullable(this.id).orElse(entity.getId()))
                .name(Optional.ofNullable(this.name).orElse(entity.getName()))
                .objectName(Optional.ofNullable(this.objectName).orElse(entity.getObjectName()))
                .objectAddress(Optional.ofNullable(this.objectAddress).orElse(entity.getObjectAddress()))
                .releaseDate(Optional.ofNullable(this.releaseDate).orElse(entity.getReleaseDate()))
                .volumeNumber(Optional.ofNullable(this.volumeNumber).orElse(entity.getVolumeNumber()))
                .volumeName(Optional.ofNullable(this.volumeName).orElse(entity.getVolumeName()))
                .code(Optional.ofNullable(this.code).orElse(entity.getCode()))
                .designer(Optional.ofNullable(this.designerId).map(x -> UserEntity.builder().id(x).build()).orElse(entity.getDesigner()))
                .supervisor(Optional.ofNullable(this.supervisorId).map(x -> UserEntity.builder().id(x).build()).orElse(entity.getSupervisor()))
                .chief(Optional.ofNullable(this.chiefId).map(x -> UserEntity.builder().id(x).build()).orElse(entity.getChief()))
                .controller(Optional.ofNullable(this.controllerId).map(x -> UserEntity.builder().id(x).build()).orElse(entity.getController()))
                .company(Optional.ofNullable(this.companyId).map(x -> CompanyEntity.builder().id(x).build()).orElse(entity.getCompany()))
                .stage(Optional.ofNullable(this.stageId).map(x -> StageEntity.builder().id(x).build()).orElse(entity.getStage()))
                .editTime(LocalDateTime.now())
                .reassemblyRequired(true)
                .build();
    }
}
