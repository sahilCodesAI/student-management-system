package com.sms.student;

import com.sms.student.dto.StudentRequestDto;
import com.sms.student.dto.StudentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface — the contract for all student business operations.
 *
 * KEY CONCEPT — Why define an interface instead of just a class?
 *
 * 1. LOOSE COUPLING (Dependency Inversion Principle)
 *    The Controller depends on this INTERFACE, not on StudentServiceImpl.
 *    This means you can swap the implementation without touching the controller.
 *
 * 2. TESTABILITY
 *    In unit tests, you can mock this interface with Mockito instead of
 *    needing a real database or Spring context.
 *
 * 3. CLARITY OF CONTRACT
 *    Anyone reading the codebase can understand what the service does just
 *    by reading this interface — without wading through implementation details.
 *
 * PATTERN: Program to an interface, not to an implementation.
 * This is one of the most fundamental principles in Java/Spring development.
 */
public interface StudentService {

    /**
     * Creates a new student from the given request data.
     *
     * @param requestDto validated input from the client
     * @return the created student as a response DTO (includes generated ID, timestamps)
     * @throws com.sms.exception.DuplicateResourceException if email is already taken
     */
    StudentResponseDto createStudent(StudentRequestDto requestDto);

    /**
     * Retrieves a single student by their ID.
     *
     * @param id the student's primary key
     * @return the student as a response DTO
     * @throws com.sms.exception.ResourceNotFoundException if no student exists with that ID
     */
    StudentResponseDto getStudentById(Long id);

    /**
     * Retrieves a paginated, sorted list of all students.
     *
     * @param pageable encapsulates page number, page size, and sort information
     * @return a Page containing the student DTOs and pagination metadata
     */
    Page<StudentResponseDto> getAllStudents(Pageable pageable);

    /**
     * Fully updates an existing student (all fields are replaced).
     *
     * @param id         the ID of the student to update
     * @param requestDto the new data for the student
     * @return the updated student as a response DTO
     * @throws com.sms.exception.ResourceNotFoundException  if no student exists with that ID
     * @throws com.sms.exception.DuplicateResourceException if the new email belongs to someone else
     */
    StudentResponseDto updateStudent(Long id, StudentRequestDto requestDto);

    /**
     * Deletes a student by their ID.
     *
     * @param id the ID of the student to delete
     * @throws com.sms.exception.ResourceNotFoundException if no student exists with that ID
     */
    void deleteStudent(Long id);
}
