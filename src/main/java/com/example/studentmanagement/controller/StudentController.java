package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.StudentRequestDto;
import com.example.studentmanagement.dto.StudentResponseDto;
import com.example.studentmanagement.service.StudentService;
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
 * Layer role: controller/ receives HTTP requests and returns HTTP responses.
 *   It knows: verbs (GET/POST/PUT/DELETE), status codes, request/response bodies.
 *   It does NOT know: SQL, business rules, entities.
 *
 * @RestController = @Controller + @ResponseBody
 *   Every method return value is serialised to JSON automatically by Jackson.
 *
 * @RequestMapping("/api/v1/students")
 *   Base URL prefix applied to all methods in this class.
 *
 * @RequiredArgsConstructor
 *   Constructor injection of StudentService (the interface, not the impl).
 *   The controller is decoupled from StudentServiceImpl — it only knows the contract.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    // ── POST /api/v1/students ────────────────────────────────────────────────
    // @RequestBody  → deserialises the JSON body into a StudentRequestDto
    // @Valid        → triggers Bean Validation on the DTO before the method runs;
    //                 if validation fails, MethodArgumentNotValidException is thrown
    //                 and GlobalExceptionHandler returns a 400 with field errors.
    @PostMapping
    public ResponseEntity<StudentResponseDto> createStudent(
            @Valid @RequestBody StudentRequestDto requestDto) {

        StudentResponseDto response = studentService.createStudent(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201 Created
    }

    // ── GET /api/v1/students/{id} ────────────────────────────────────────────
    // @PathVariable → extracts the {id} segment from the URL path
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDto> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id)); // 200 OK
    }

    // ── GET /api/v1/students?page=0&size=10&sortBy=id&sortDir=asc ───────────
    // @RequestParam → binds a URL query parameter to a method argument
    // defaultValue  → used when the client omits the parameter
    @GetMapping
    public ResponseEntity<Page<StudentResponseDto>> getAllStudents(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "id")  String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort.Direction direction = Sort.Direction.fromOptionalString(sortDir)
                .orElse(Sort.Direction.ASC);

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(studentService.getAllStudents(pageable)); // 200 OK
    }

    // ── PUT /api/v1/students/{id} ────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDto> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequestDto requestDto) {

        return ResponseEntity.ok(studentService.updateStudent(id, requestDto)); // 200 OK
    }

    // ── DELETE /api/v1/students/{id} ─────────────────────────────────────────
    // ResponseEntity<Void> → no body in the response (the resource no longer exists)
    // 204 No Content is the correct status for a successful delete.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
