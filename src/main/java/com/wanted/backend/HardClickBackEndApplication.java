package com.wanted.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HardClickBackEndApplication {

    public static void main(String[] args) {
        SpringApplication.run(HardClickBackEndApplication.class, args);
    }

}
