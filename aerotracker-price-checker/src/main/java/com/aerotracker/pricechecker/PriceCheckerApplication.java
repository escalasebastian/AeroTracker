package com.aerotracker.pricechecker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.aerotracker.entity")
@EnableJpaRepositories("com.aerotracker.repository")
public class PriceCheckerApplication {
    public static void main(String[] args) {
        SpringApplication.run(PriceCheckerApplication.class, args);
    }
}
