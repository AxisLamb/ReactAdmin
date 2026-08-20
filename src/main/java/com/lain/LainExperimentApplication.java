package com.lain;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAdminServer
public class LainExperimentApplication {

    public static void main(String[] args) {
        SpringApplication.run(LainExperimentApplication.class, args);
    }
}
