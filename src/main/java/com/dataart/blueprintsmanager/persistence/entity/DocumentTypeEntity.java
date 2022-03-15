package com.dataart.blueprintsmanager.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "bpm_document_type")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DocumentTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "type", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private DocumentType type;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "first_page_template_id")
    private FileEntity firstPageFile;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "general_page_template_id")
    private FileEntity generalPageFile;

    @Column(name = "unmodified")
    private Boolean unmodified;

    @Column(name = "default_page_number")
    private Integer defaultPageNumber;
}
