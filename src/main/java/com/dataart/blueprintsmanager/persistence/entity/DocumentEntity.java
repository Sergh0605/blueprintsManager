package com.dataart.blueprintsmanager.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "bpm_document")
public class DocumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    @Column(name = "number_in_project", nullable = false)
    private Integer numberInProject;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private DocumentTypeEntity documentType;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(name = "designer_id")
    private UserEntity designer;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private UserEntity supervisor;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "content_in_pdf_id")
    private FileEntity contentFile;

    @Column(name = "reassembly_required", nullable = false)
    private Boolean reassemblyRequired;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "document_in_pdf_id")
    private FileEntity documentFile;

    @Column(name = "edit_time")
    @UpdateTimestamp
    private LocalDateTime editTime;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

}
