package com.example.studentmanagement.repository;

import com.example.studentmanagement.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access layer for Student.
 *
 * Layer role: repository/ contains Spring Data interfaces.
 * Spring generates the implementation at runtime — no class needed.
 *
 * JpaRepository<Student, Long> provides for free:
 *   save(entity)           → INSERT or UPDATE
 *   findById(id)           → SELECT WHERE id=? → Optional<Student>
 *   findAll(pageable)      → SELECT ... LIMIT ? OFFSET ? ORDER BY ?
 *   existsById(id)         → SELECT COUNT(*) WHERE id=?
 *   deleteById(id)         → DELETE WHERE id=?
 *
 * Custom derived query methods below — Spring parses the method name
 * and generates the SQL automatically. No @Query annotation needed.
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // → SELECT COUNT(*) FROM students WHERE email = ?
    boolean existsByEmail(String email);

    // → SELECT COUNT(*) FROM students WHERE email = ? AND id != ?
    // Used in update: lets the student keep their own email without triggering
    // a duplicate error, but blocks stealing another student's email.
    boolean existsByEmailAndIdNot(String email, Long id);

    // → SELECT * FROM students WHERE email = ?
    Optional<Student> findByEmail(String email);
}
