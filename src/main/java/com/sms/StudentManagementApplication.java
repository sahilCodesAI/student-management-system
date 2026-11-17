package com.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 *
 * @SpringBootApplication is a convenience annotation that combines:
 *
 *   1. @Configuration
 *      → Marks this class as a source of Spring bean definitions.
 *
 *   2. @EnableAutoConfiguration
 *      → Tells Spring Boot to automatically configure beans based on
 *        the dependencies on the classpath. For example:
 *        - Sees mysql-connector-j + spring-data-jpa → configures a DataSource
 *        - Sees spring-boot-starter-web            → configures DispatcherServlet
 *
 *   3. @ComponentScan
 *      → Scans this package and all sub-packages for Spring components
 *        (@Component, @Service, @Repository, @Controller, @RestController, etc.)
 *        and registers them as beans in the Spring ApplicationContext.
 *
 * The main method delegates to SpringApplication.run(), which:
 *   - Creates the ApplicationContext
 *   - Runs auto-configuration
 *   - Starts the embedded Tomcat server
 */
@SpringBootApplication
public class StudentManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentManagementApplication.class, args);
    }
}
