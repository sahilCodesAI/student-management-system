package com.example.studentmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 *
 * @SpringBootApplication combines three annotations:
 *
 *   @Configuration          → this class can define Spring beans
 *   @EnableAutoConfiguration → auto-configures beans from classpath dependencies
 *                              (sees mysql driver + JPA → sets up DataSource)
 *   @ComponentScan          → scans THIS package and all sub-packages for:
 *                              @Service, @Repository, @RestController, etc.
 *
 * Because this class lives in com.example.studentmanagement, Spring will
 * automatically find all beans inside:
 *   com.example.studentmanagement.controller
 *   com.example.studentmanagement.service
 *   com.example.studentmanagement.repository
 *   com.example.studentmanagement.exception
 *   ...and so on.
 */
@SpringBootApplication
public class StudentManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentManagementApplication.class, args);
    }
}
