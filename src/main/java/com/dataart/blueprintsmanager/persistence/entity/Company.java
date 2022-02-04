package com.dataart.blueprintsmanager.persistence.entity;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Company {
    Long id;
    String name;
    String signerPosition;
    String signerName;
    byte[] logo;
}
