package com.smarttravel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * SmartTravel Platform Application Entry Point
 * 
 * Enterprise full-stack travel booking platform featuring real-time flight tracking,
 * dynamic pricing, automated cancellation/refunds, seat/room selection, 
 * verified reviews, and personalized recommendations.
 */
@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class })
@EnableScheduling
@EnableAsync
@org.springframework.cache.annotation.EnableCaching
public class SmartTravelApplication {

    static {
        loadDotenv();
    }

    public static void main(String[] args) {
        loadDotenv();
        SpringApplication.run(SmartTravelApplication.class, args);
    }

    /**
     * Safely loads local environment variables from .env file if present.
     * Never logs sensitive values or overrides existing system environment variables.
     */
    public static void loadDotenv() {
        File[] possibleFiles = new File[]{
                new File(".env"),
                new File("backend/.env"),
                new File("../backend/.env")
        };

        for (File file : possibleFiles) {
            if (file.exists() && file.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        int eqIdx = line.indexOf('=');
                        if (eqIdx > 0) {
                            String key = line.substring(0, eqIdx).trim();
                            String value = line.substring(eqIdx + 1).trim();
                            if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                                value = value.substring(1, value.length() - 1);
                            }
                            if (System.getProperty(key) == null && System.getenv(key) == null) {
                                System.setProperty(key, value);
                            }
                        }
                    }
                } catch (Exception ignored) {
                    // Fallback to standard Spring Boot property resolution
                }
                break;
            }
        }
    }
}
