package com.dataart.blueprintsmanager.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "bpm_company")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CompanyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "signer_position", nullable = false)
    private String signerPosition;

    @Column(name = "signer_name", nullable = false)
    private String signerName;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "logo_file_id")
    private FileEntity logoFile;

    @Column(name = "city")
    private String city;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;
}
