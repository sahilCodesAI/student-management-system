package com.sms.student;

import com.sms.student.dto.StudentRequestDto;
import com.sms.student.dto.StudentResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller — the HTTP entry point for all student operations.
 *
 * KEY ANNOTATIONS:
 *
 * @RestController → combines @Controller + @ResponseBody.
 *   @Controller    = marks this as a Spring MVC controller (handles HTTP requests)
 *   @ResponseBody  = serialises every return value to JSON automatically.
 *   Without @ResponseBody, Spring would try to find a view template (Thymeleaf etc.)
 *
 * @RequestMapping("/api/v1/students") → base URL path for all methods in this class.
 *   /api    → signals this is an API, not a webpage
 *   /v1     → version prefix (makes future API versioning easier)
 *   /students → the resource name (plural noun, REST convention)
 *
 * @RequiredArgsConstructor (Lombok) → constructor injection of StudentService.
 *   The controller depends on the INTERFACE (StudentService), not the implementation.
 *   This is the Dependency Inversion Principle — the controller is decoupled from
 *   the concrete class, making it easy to swap or mock in tests.
 *
 * RULE: Controllers know HTTP. They must NOT contain business logic or DB calls.
 *       All logic is delegated to the service layer.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/v1/students
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new student.
     *
     * @PostMapping → handles HTTP POST requests to /api/v1/students
     *
     * @RequestBody → deserialises the incoming JSON body into a StudentRequestDto object.
     *   Jackson (bundled with Spring Web) performs the JSON → Java conversion.
     *
     * @Valid → triggers Bean Validation on the StudentRequestDto BEFORE the method runs.
     *   If any constraint (@NotBlank, @Email, etc.) fails, Spring throws
     *   MethodArgumentNotValidException — our GlobalExceptionHandler catches it.
     *
     * ResponseEntity<T> → gives us full control over the HTTP response:
     *   - status code (201 Created, not just 200 OK)
     *   - response body
     *   - headers (if needed)
     *
     * HTTP 201 Created is the correct status for a successful resource creation.
     */
    @PostMapping
    public ResponseEntity<StudentResponseDto> createStudent(
            @Valid @RequestBody StudentRequestDto requestDto) {

        StudentResponseDto response = studentService.createStudent(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/students/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves a single student by ID.
     *
     * @GetMapping("/{id}") → handles GET /api/v1/students/42
     *
     * @PathVariable → extracts the {id} segment from the URL path.
     *   The parameter name must match the template variable name, or use
     *   @PathVariable("id") Long studentId if names differ.
     *
     * HTTP 200 OK is the default — ResponseEntity.ok() is a shorthand.
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDto> getStudentById(@PathVariable Long id) {
        StudentResponseDto response = studentService.getStudentById(id);
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/students?page=0&size=10&sortBy=lastName&sortDir=asc
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves all students with pagination and sorting.
     *
     * @RequestParam → binds a query parameter from the URL to a method parameter.
     *   defaultValue → used if the client doesn't send that parameter.
     *
     * WHY MANUAL Pageable INSTEAD OF @PageableDefault?
     * Building Pageable manually gives us full control over:
     *   - Sanitising the sortBy field (prevent SQL injection via column names)
     *   - Defaulting to ascending if an invalid direction is given
     *   - Validating page/size ranges
     *
     * Sort.Direction.fromOptionalString() → safely parses "asc"/"desc".
     *   Returns Optional<Direction>, defaults to ASC if the string is invalid.
     *
     * PageRequest.of(page, size, sort) → creates a Pageable object that Spring Data
     *   passes directly to the repository's findAll(Pageable) method.
     */
    @GetMapping
    public ResponseEntity<Page<StudentResponseDto>> getAllStudents(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "id")  String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort.Direction direction = Sort.Direction.fromOptionalString(sortDir)
                .orElse(Sort.Direction.ASC);

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<StudentResponseDto> response = studentService.getAllStudents(pageable);
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/v1/students/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fully updates a student (all fields replaced).
     *
     * @PutMapping("/{id}") → handles PUT /api/v1/students/42
     *
     * PUT vs PATCH:
     *   PUT   → replace the entire resource (client sends ALL fields)
     *   PATCH → partial update (client sends only the fields to change)
     *   We use PUT here for simplicity — same DTO as create.
     */
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDto> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequestDto requestDto) {

        StudentResponseDto response = studentService.updateStudent(id, requestDto);
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/v1/students/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Deletes a student by ID.
     *
     * @DeleteMapping("/{id}") → handles DELETE /api/v1/students/42
     *
     * HTTP 204 No Content → the correct status for a successful DELETE.
     * The response has NO body (the resource no longer exists, there's nothing to return).
     * ResponseEntity<Void> communicates this explicitly — the type parameter is Void.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}
