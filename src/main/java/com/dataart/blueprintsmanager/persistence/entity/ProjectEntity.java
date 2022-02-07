package com.dataart.blueprintsmanager.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProjectEntity {
    private Long id;
    private String name;
    private String objectName;
    private String objectAddress;
    private LocalDate releaseDate;
    private Integer volumeNumber;
    private String volumeName;
    private String code;
    private UserEntity designer;
    private UserEntity supervisor;
    private UserEntity chief;
    private UserEntity controller;
    private CompanyEntity company;
    private StageEntity stage;
    private Boolean reassemblyRequired;
    private byte[] projectInPdf;
    private LocalDateTime editTime;
}
