package com.x1.groo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GrooApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrooApplication.class, args);
    }
}
