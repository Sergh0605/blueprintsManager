package com.dataart.blueprintsmanager.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CompanyDataForPdf {
    private String name;
    private byte[] logo;
    private String city;
    private String signerPosition;
    private String signerName;

    public void setName(String name) {
        if (name != null) {
            this.name = name.trim();
            if (this.name.length() > 11) {
                this.name = this.name.substring(0, 11);
            }
        }
    }
}
