package com.sms.student;

import com.sms.exception.DuplicateResourceException;
import com.sms.exception.ResourceNotFoundException;
import com.sms.student.dto.StudentRequestDto;
import com.sms.student.dto.StudentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic implementation for student operations.
 *
 * KEY ANNOTATIONS:
 *
 * @Service → a specialisation of @Component.
 *   Marks this as a Spring-managed bean that lives in the service layer.
 *   Spring will detect it during component scan and register it in the context.
 *
 * @RequiredArgsConstructor (Lombok) → generates a constructor for all 'final' fields.
 *   Spring uses constructor injection (preferred over @Autowired field injection) to
 *   inject the StudentRepository. Constructor injection:
 *     - Makes dependencies explicit and mandatory
 *     - Easier to test (just call new StudentServiceImpl(mockRepo))
 *     - Prevents circular dependency issues
 *
 * @Transactional → Spring wraps the annotated method in a database transaction.
 *   If any exception is thrown, the transaction is rolled back automatically.
 *   Class-level @Transactional(readOnly = true) sets the default for all methods,
 *   then we override it with @Transactional (readOnly = false) on write methods.
 *   readOnly = true is a performance hint to Hibernate: skip dirty checking.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // default: all methods are read-only transactions
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @Transactional overrides the class-level readOnly=true because this
     * method writes to the database (INSERT statement).
     */
    @Override
    @Transactional
    public StudentResponseDto createStudent(StudentRequestDto requestDto) {
        // 1. Business rule: email must be unique
        //    We check here (service layer) to throw a friendly exception.
        //    The DB unique constraint is a last line of defence.
        if (studentRepository.existsByEmail(requestDto.getEmail())) {
            throw new DuplicateResourceException(
                "Student with email '" + requestDto.getEmail() + "' already exists"
            );
        }

        // 2. Map RequestDto → Entity
        //    We do this manually so you can see exactly what's happening.
        //    Notice: we do NOT set id, createdAt, or updatedAt —
        //    those are managed by JPA/Hibernate automatically.
        Student student = Student.builder()
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .email(requestDto.getEmail())
                .phone(requestDto.getPhone())
                .age(requestDto.getAge())
                .course(requestDto.getCourse())
                .build();

        // 3. Persist: repository.save() triggers an INSERT statement.
        //    The returned 'savedStudent' has the generated id, createdAt, updatedAt.
        Student savedStudent = studentRepository.save(student);

        // 4. Map Entity → ResponseDto and return
        return mapToResponseDto(savedStudent);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ ONE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public StudentResponseDto getStudentById(Long id) {
        // findById returns Optional<Student>.
        // orElseThrow() unwraps the Optional: if empty, throws our custom exception.
        // This is far cleaner than: if (student == null) throw new ...
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Student not found with id: " + id
                ));

        return mapToResponseDto(student);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ ALL (PAGINATED + SORTED)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public Page<StudentResponseDto> getAllStudents(Pageable pageable) {
        // findAll(Pageable) generates: SELECT * FROM students LIMIT ? OFFSET ? ORDER BY ?
        // It returns Page<Student> — we map each Student to a StudentResponseDto.
        //
        // Page.map() applies the mapper function to every element in the page
        // while preserving all pagination metadata (totalElements, totalPages, etc.)
        return studentRepository.findAll(pageable)
                .map(this::mapToResponseDto);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public StudentResponseDto updateStudent(Long id, StudentRequestDto requestDto) {
        // 1. Verify the student exists
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Student not found with id: " + id
                ));

        // 2. Check email uniqueness — but allow the student to keep their OWN email.
        //    existsByEmailAndIdNot: "does ANY student OTHER than this one have this email?"
        if (studentRepository.existsByEmailAndIdNot(requestDto.getEmail(), id)) {
            throw new DuplicateResourceException(
                "Email '" + requestDto.getEmail() + "' is already in use by another student"
            );
        }

        // 3. Update the entity's fields.
        //    We're modifying the 'existingStudent' object which Hibernate is tracking.
        //    Since we're inside a @Transactional method, Hibernate's "dirty checking"
        //    will automatically detect the changes and issue an UPDATE at commit time —
        //    we don't even need to call save() explicitly!
        //    (We do call save() here for explicitness and to get the updated entity back.)
        existingStudent.setFirstName(requestDto.getFirstName());
        existingStudent.setLastName(requestDto.getLastName());
        existingStudent.setEmail(requestDto.getEmail());
        existingStudent.setPhone(requestDto.getPhone());
        existingStudent.setAge(requestDto.getAge());
        existingStudent.setCourse(requestDto.getCourse());

        // 4. save() on an existing entity (has an ID) → triggers an UPDATE statement.
        //    @UpdateTimestamp will automatically refresh updatedAt.
        Student updatedStudent = studentRepository.save(existingStudent);

        return mapToResponseDto(updatedStudent);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        // First verify the student exists — if not, throw 404.
        // deleteById() does NOT throw a meaningful exception if ID doesn't exist,
        // it throws EmptyResultDataAccessException which would map to 500.
        // Our manual check gives the client a proper 404 with a clear message.
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student not found with id: " + id);
        }

        studentRepository.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPER — Entity → ResponseDto mapping
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Converts a Student entity into a StudentResponseDto.
     *
     * This is the manual mapping you'd eventually replace with MapStruct.
     * It's a private method because mapping is an implementation detail —
     * the controller and tests don't need to know how it works.
     *
     * 'this::mapToResponseDto' is a method reference used in Page.map() above.
     */
    private StudentResponseDto mapToResponseDto(Student student) {
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
