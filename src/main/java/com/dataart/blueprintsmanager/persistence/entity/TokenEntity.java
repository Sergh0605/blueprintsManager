package com.dataart.blueprintsmanager.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bpm_token")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TokenEntity {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "exp_datetime")
    private LocalDateTime expDateTime;

    @Column(name = "disabled")
    private Boolean disabled;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
