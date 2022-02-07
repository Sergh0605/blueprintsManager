package com.dataart.blueprintsmanager.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    private Long id;
    private String lastName;
    private String login;
    private String password;
    private Company company;
    private byte[] signature;
}
