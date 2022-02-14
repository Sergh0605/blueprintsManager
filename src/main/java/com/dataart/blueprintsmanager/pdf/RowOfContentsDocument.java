package com.dataart.blueprintsmanager.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RowOfContentsDocument {
    private String column1;
    private String column2;
    private String column3;
}
