package com.gumraze.drive.drive_backend.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "region")
public class Region {
    @Id
    private Long id;

    private Long parentId;

    private String regionName;
    private String regionCode;
}
