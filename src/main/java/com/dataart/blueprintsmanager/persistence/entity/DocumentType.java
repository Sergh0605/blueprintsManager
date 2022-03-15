package com.dataart.blueprintsmanager.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentType {
    COVER_PAGE,
    TITLE_PAGE,
    TABLE_OF_CONTENTS,
    GENERAL_INFORMATION,
    DRAWING;
}
