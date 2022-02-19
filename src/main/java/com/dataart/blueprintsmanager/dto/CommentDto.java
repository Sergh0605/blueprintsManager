package com.dataart.blueprintsmanager.dto;

import com.dataart.blueprintsmanager.persistence.entity.CommentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommentDto {
    static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private Long id;
    private String login;
    private String text;
    private LocalDateTime publicationTime;
    private Long projectId;
    private Long documentId;

    public CommentDto(CommentEntity comment) {
        if (comment != null) {
            this.id = comment.getId();
            this.login = Optional.ofNullable(comment.getUser()).map(user -> Optional.ofNullable(user.getLogin()).orElse("LOGIN NOT FOUND")).orElse("USER NOT FOUND");
            this.text = comment.getText();
            this.publicationTime = comment.getPublicationDateTime();
            this.projectId = comment.getProject().getId();
            this.documentId = comment.getDocument().getId();
        }
    }

    public String getPublicationTimeWithFormat() {
        return getPublicationTime().format(dateTimeFormatter).toUpperCase(Locale.ROOT);
    }
}