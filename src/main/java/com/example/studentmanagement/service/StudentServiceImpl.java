package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.StudentRequestDto;
import com.example.studentmanagement.dto.StudentResponseDto;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.exception.DuplicateResourceException;
import com.example.studentmanagement.exception.ResourceNotFoundException;
import com.example.studentmanagement.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * StudentService implementation — contains all business logic for student operations.
 *
 * @Service
 *   Marks this as a Spring-managed bean in the service layer.
 *   Spring registers it in the ApplicationContext during startup.
 *
 * @RequiredArgsConstructor (Lombok)
 *   Generates a constructor for every 'private final' field.
 *   Spring sees the single constructor and uses it for dependency injection
 *   automatically — no @Autowired needed.
 *   Constructor injection is preferred because:
 *     ✔ Dependencies are explicit (declared in constructor signature)
 *     ✔ Fields can be 'final' (immutable after construction)
 *     ✔ Easy to unit-test: new StudentServiceImpl(mockRepository)
 *
 * @Transactional(readOnly = true)  [class level]
 *   Wraps every method in a read-only transaction by default.
 *   readOnly = true → Hibernate skips dirty checking (performance gain).
 *   Write methods (@Transactional without readOnly) override this below.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {

    // 'final' → must be set by the constructor, can never be reassigned.
    // @RequiredArgsConstructor sees this and includes it in the generated constructor.
    private final StudentRepository studentRepository;

    // ── CREATE ───────────────────────────────────────────────────────────────

    @Override
    @Transactional          // overrides readOnly=true because we write to DB
    public StudentResponseDto createStudent(StudentRequestDto requestDto) {

        // Business rule: no two students can share an email.
        // We check here (service layer) to return a friendly 409 message.
        // The DB unique constraint is a last line of defence if we ever bypass this.
        if (studentRepository.existsByEmail(requestDto.getEmail())) {
            throw new DuplicateResourceException(
                    "Student with email '" + requestDto.getEmail() + "' already exists"
            );
        }

        // Map RequestDto → Entity.
        // We do NOT set 'id' — Hibernate generates it via AUTO_INCREMENT after INSERT.
        Student student = Student.builder()
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .email(requestDto.getEmail())
                .phone(requestDto.getPhone())
                .age(requestDto.getAge())
                .course(requestDto.getCourse())
                .build();

        // save() on a new entity (id = null) → triggers an INSERT statement.
        // The returned object has the generated id populated.
        Student saved = studentRepository.save(student);

        return toResponseDto(saved);
    }

    // ── READ ONE ─────────────────────────────────────────────────────────────

    @Override
    public StudentResponseDto getStudentById(Long id) {

        // findById returns Optional<Student> — never null.
        // orElseThrow() extracts the value or throws our exception if empty.
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id: " + id
                ));

        return toResponseDto(student);
    }

    // ── READ ALL (paginated + sorted) ────────────────────────────────────────

    @Override
    public Page<StudentResponseDto> getAllStudents(Pageable pageable) {

        // findAll(Pageable) → SELECT * FROM students LIMIT ? OFFSET ? ORDER BY ?
        // Page<Student>.map() converts each Student to StudentResponseDto
        // while preserving all pagination metadata (totalElements, totalPages, etc.)
        return studentRepository.findAll(pageable)
                .map(this::toResponseDto);
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public StudentResponseDto updateStudent(Long id, StudentRequestDto requestDto) {

        // 1. Confirm the student exists
        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id: " + id
                ));

        // 2. Guard: is the new email already taken by a DIFFERENT student?
        //    existsByEmailAndIdNot → "does anyone other than me have this email?"
        //    This lets the student keep their own email without triggering a conflict.
        if (studentRepository.existsByEmailAndIdNot(requestDto.getEmail(), id)) {
            throw new DuplicateResourceException(
                    "Email '" + requestDto.getEmail() + "' is already in use"
            );
        }

        // 3. Apply changes — Hibernate's dirty checking detects these mutations
        //    and issues an UPDATE SQL at transaction commit automatically.
        existing.setFirstName(requestDto.getFirstName());
        existing.setLastName(requestDto.getLastName());
        existing.setEmail(requestDto.getEmail());
        existing.setPhone(requestDto.getPhone());
        existing.setAge(requestDto.getAge());
        existing.setCourse(requestDto.getCourse());

        // save() on an existing entity (id != null) → triggers an UPDATE statement.
        Student updated = studentRepository.save(existing);

        return toResponseDto(updated);
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteStudent(Long id) {

        // Pre-check so the client gets a 404, not a cryptic Spring exception.
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student not found with id: " + id);
        }

        studentRepository.deleteById(id);
    }

    // ── PRIVATE HELPER — Entity → ResponseDto ────────────────────────────────

    private StudentResponseDto toResponseDto(Student student) {
        return StudentResponseDto.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .age(student.getAge())
                .course(student.getCourse())
                .build();
    }
}
