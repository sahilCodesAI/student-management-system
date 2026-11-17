package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.StudentRequestDto;
import com.example.studentmanagement.dto.StudentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service contract — defines WHAT the service does, not HOW.
 *
 * Layer role: service/ contains business logic.
 *   - Validates business rules (e.g. duplicate email check)
 *   - Converts between DTOs and Entities
 *   - Calls the repository for persistence
 *   - Throws domain exceptions (ResourceNotFoundException etc.)
 *
 * The Controller depends on this INTERFACE, not on the implementation class.
 * This is the Dependency Inversion Principle:
 *   "Depend on abstractions, not on concretions."
 */
public interface StudentService {

    StudentResponseDto createStudent(StudentRequestDto requestDto);

    StudentResponseDto getStudentById(Long id);

    Page<StudentResponseDto> getAllStudents(Pageable pageable);

    StudentResponseDto updateStudent(Long id, StudentRequestDto requestDto);

    void deleteStudent(Long id);
}
