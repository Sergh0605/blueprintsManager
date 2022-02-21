package com.dataart.blueprintsmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BlueprintsManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlueprintsManagerApplication.class, args);
    }

}
