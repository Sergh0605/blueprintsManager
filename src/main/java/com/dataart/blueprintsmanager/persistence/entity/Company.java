package com.dataart.blueprintsmanager.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Company {
    private Long id;
    private String name;
    private String signerPosition;
    private String signerName;
    private byte[] logo;
}
