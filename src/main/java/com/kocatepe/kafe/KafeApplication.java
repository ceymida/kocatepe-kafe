package com.kocatepe.kafe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class KafeApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafeApplication.class, args);
    }
}
