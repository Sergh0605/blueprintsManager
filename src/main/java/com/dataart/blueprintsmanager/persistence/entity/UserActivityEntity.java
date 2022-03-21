package com.dataart.blueprintsmanager.persistence.entity;

import com.dataart.blueprintsmanager.aop.track.UserAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bpm_user_activity")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserActivityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "login")
    private String login;

    @Column(name = "action")
    @Enumerated(EnumType.STRING)
    private UserAction action;

    @Column(name = "message")
    private String message;

    @Column(name = "timestamp")
    @UpdateTimestamp
    private LocalDateTime timestamp;
}
