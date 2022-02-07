package com.dataart.blueprintsmanager.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommentEntity {
    private Long id;
    private UserEntity user;
    private ProjectEntity project;
    private DocumentEntity document;
    private String text;
    private LocalDateTime publicationDateTime;
}
