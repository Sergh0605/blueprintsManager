package com.dataart.blueprintsmanager.dto;

import com.dataart.blueprintsmanager.persistence.entity.CompanyEntity;
import com.dataart.blueprintsmanager.persistence.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDto {
    private Long id;
    private String name;
    private String login;
    private String companyName;
    private boolean hasSign;

    public UserDto(UserEntity user) {
        if (user != null) {
            this.id = user.getId();
            this.name = user.getLastName();
            this.login = Optional.ofNullable(user.getLogin()).orElse("");
            this.companyName = Optional.ofNullable(user.getCompany()).map(CompanyEntity::getName).orElse("");
            if (user.getSignature() != null && user.getSignature().length > 0) {
                this.hasSign = true;
            }
            if (user.getSignature() != null && user.getSignature().length > 0) {
                this.hasSign = true;
            }
        }
    }
}
