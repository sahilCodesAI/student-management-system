package com.example.studentmanagement.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Incoming request payload from the client.
 *
 * Layer role: dto/ contains classes that cross the API boundary.
 *   - RequestDto  → client sends this (controller receives it)
 *   - ResponseDto → client receives this (controller returns it)
 *
 * NEVER put @Entity or @Column here.
 * NEVER send a raw Entity to/from the controller.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentRequestDto {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 150, message = "Age must be realistic")
    private Integer age;

    @Size(max = 150, message = "Course must not exceed 150 characters")
    private String course;
}
