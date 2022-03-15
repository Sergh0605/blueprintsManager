package com.dataart.blueprintsmanager.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommentDto {
    private Long id;
    @Valid
    private BasicDto user;
    @NotEmpty
    private String text;
    private LocalDateTime publicationDateTime;
    @Valid
    private BasicDto project;
    @Valid
    private BasicDto document;
}
