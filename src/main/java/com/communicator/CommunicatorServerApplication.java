package com.communicator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring boot application file.
 */

@SuppressWarnings("PMD")
@SpringBootApplication
public class CommunicatorServerApplication {

    /**
     * Spring Boot application runner.
     * @param args runtime arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CommunicatorServerApplication.class, args);
    }

}