package com.dataart.blueprintsmanager.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProjectDto {
    private Long id;
    @NotEmpty
    private String name;
    private String objectName;
    private String objectAddress;
    @NotNull
    private LocalDate releaseDate;
    @NotNull
    private Long volumeNumber;
    @NotEmpty
    private String volumeName;
    @NotEmpty
    private String code;
    @Valid
    private BasicDto designer;
    @Valid
    private BasicDto supervisor;
    @Valid
    private BasicDto chief;
    @Valid
    private BasicDto controller;
    @NotNull
    @Valid
    private BasicDto company;
    private Boolean reassemblyRequired;
    private LocalDateTime editTime;
    @NotNull
    @Valid
    private BasicDto stage;
    private Boolean deleted;
    @NotNull
    private Long version;
}
