package edu.unicauca.progresstracking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProgressTrackingApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProgressTrackingApplication.class, args);
        System.out.println("🚀 Progress Tracking Service is running...");
    }
}
