package com.dataart.blueprintsmanager.dto;

import com.dataart.blueprintsmanager.persistence.entity.CompanyEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CompanyDto {
    private Long id;
    private String name;
    private String signerPosition;
    private String signerName;
    private boolean hasLogo;
    private String city;

    public CompanyDto(CompanyEntity company) {
        if (company != null) {
            this.id = company.getId();
            this.name = company.getName();
            this.signerPosition = company.getSignerPosition();
            this.signerName = company.getSignerName();
            this.city = company.getCity();
            if (company.getLogo() != null && company.getLogo().length > 0) {
                hasLogo = true;
            }
        }
    }
}


