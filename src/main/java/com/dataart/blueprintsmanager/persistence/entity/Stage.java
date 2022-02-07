package com.dataart.blueprintsmanager.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Stage {
    private Long id;
    private String name;
    private String code;
}
