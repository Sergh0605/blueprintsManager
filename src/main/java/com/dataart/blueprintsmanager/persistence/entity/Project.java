package com.dataart.blueprintsmanager.persistence.entity;

import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Project {
    Long id;
    String name;
    String objectName;
    String objectAddress;
    LocalDate releaseDate;
    Integer volumeNumber;
    String volumeName;
    String code;
    User designerId;
    User supervisorId;
    User chiefId;
    User controllerId;
    Company company;
    Stage stage;
    boolean reassemblyRequired;
    byte[] projectInPdf;
    Date editTime;
}
