package com.gumraze.drive.drive_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DriveBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DriveBackendApplication.class, args);
    }
}
