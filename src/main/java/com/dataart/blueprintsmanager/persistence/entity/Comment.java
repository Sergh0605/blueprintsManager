package com.dataart.blueprintsmanager.persistence.entity;

import lombok.*;

import java.util.Date;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Comment {
    private Long id;
    private User user;
    private Project project;
    private Document document;
    private String text;
    private Date publicationDate;
}
