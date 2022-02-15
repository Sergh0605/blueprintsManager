package com.dataart.blueprintsmanager.persistence.entity;

import com.dataart.blueprintsmanager.exceptions.NotFoundCustomApplicationException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum DocumentType {
    COVER_PAGE(1L, "Cover page", true),
    TITLE_PAGE(2L, "Title page", true),
    TABLE_OF_CONTENTS(3L, "Contents", true),
    GENERAL_INFORMATION(4L, "General information", false),
    DRAWING(5L, "Drawing", false);

    private final Long id;
    private final String name;
    private final boolean unmodified;

    public static DocumentType getById(Long id) {
        return Stream.of(values())
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NotFoundCustomApplicationException(String.format("DocumentType with id = [%d] not found", id)));
    }
}
