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
public class Comment {
    private Long id;
    private User user;
    private Project project;
    private Document document;
    private String text;
    private LocalDateTime publicationDateTime;
}
