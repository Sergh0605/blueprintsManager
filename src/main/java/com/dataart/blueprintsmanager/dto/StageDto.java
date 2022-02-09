package com.dataart.blueprintsmanager.dto;

import com.dataart.blueprintsmanager.persistence.entity.StageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StageDto {
    private Long id;
    private String name;
    private String code;

    public StageDto(StageEntity stage) {
        if (stage != null) {
            this.id = stage.getId();
            this.name = stage.getName();
            this.code = stage.getCode();
        }
    }
}
