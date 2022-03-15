package com.dataart.blueprintsmanager.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CompanyDto {
    private Long id;
    @NotEmpty
    private String name;
    @NotEmpty
    private String signerPosition;
    @NotEmpty
    private String signerName;
    private String city;
    private Boolean deleted;
}


