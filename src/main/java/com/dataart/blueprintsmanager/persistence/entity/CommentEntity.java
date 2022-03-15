package com.dataart.blueprintsmanager.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bpm_comment")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private DocumentEntity document;

    @Column(name = "content", nullable = false)
    private String text;

    @Column(name = "publication_time")
    @CreationTimestamp
    private LocalDateTime publicationDateTime;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;
}
