package com.dataart.blueprintsmanager.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bpm_project")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "object_name")
    private String objectName;

    @Column(name = "object_address")
    private String objectAddress;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "volume_number")
    private Long volumeNumber;

    @Column(name = "subname")
    private String volumeName;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @ManyToOne
    @JoinColumn(name = "designer_id")
    private UserEntity designer;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private UserEntity supervisor;

    @ManyToOne
    @JoinColumn(name = "chief_id")
    private UserEntity chief;

    @ManyToOne
    @JoinColumn(name = "controller_id")
    private UserEntity controller;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private CompanyEntity company;

    @ManyToOne
    @JoinColumn(name = "stage_id")
    private StageEntity stage;

    @Column(name = "reassembly_required", nullable = false)
    private Boolean reassemblyRequired;

    @Column(name = "edit_time")
    @UpdateTimestamp
    private LocalDateTime editTime;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;
}
