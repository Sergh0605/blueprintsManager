package com.dataart.blueprintsmanager.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "bpm_file")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "content", nullable = false)
    private byte[] fileInBytes;

    public FileEntity(byte[] fileInBytes) {
        this.fileInBytes = fileInBytes;
    }
}
