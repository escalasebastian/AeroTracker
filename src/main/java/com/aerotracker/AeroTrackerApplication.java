package com.aerotracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AeroTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AeroTrackerApplication.class, args);
    }
}
