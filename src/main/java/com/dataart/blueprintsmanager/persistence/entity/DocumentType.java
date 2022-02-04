package com.dataart.blueprintsmanager.persistence.entity;

import com.dataart.blueprintsmanager.exceptions.NotFoundCustomApplicationException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum DocumentType {
    COVER_PAGE(1L),
    TITLE_PAGE(2L),
    TABLE_OF_CONTENTS(3L),
    GENERAL_INFORMATION(4L),
    DRAWING(5L);

    private final Long id;

    public static DocumentType getById(Long id) {
        return Stream.of(values()).filter(t -> t.getId().equals(id)).findFirst().orElseThrow(() -> new NotFoundCustomApplicationException(String.format("DocumentType with id = [%d] not found", id)));
    }
}
