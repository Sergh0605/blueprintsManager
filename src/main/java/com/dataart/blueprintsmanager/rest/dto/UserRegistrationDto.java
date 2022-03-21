package com.dataart.blueprintsmanager.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserRegistrationDto {
    @NotEmpty
    private String lastName;

    @NotEmpty
    private String login;

    @NotEmpty
    private String password;

    @NotNull
    @Valid
    private BasicDto company;

    @Email
    @NotNull
    private String email;

    @Valid
    private Set<BasicDto> roles;
}
