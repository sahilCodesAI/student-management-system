package com.sms.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access layer for the Student entity.
 *
 * KEY CONCEPT — What is a Repository?
 * It is the Spring abstraction for the DAO (Data Access Object) pattern.
 * Instead of writing SQL or HQL manually, we extend JpaRepository and get
 * dozens of methods for free.
 *
 * WHY AN INTERFACE?
 * Spring Data JPA generates a proxy implementation at runtime using reflection.
 * You never write an implementing class — Spring does it for you.
 *
 * JpaRepository<Student, Long>
 *   - Student → the Entity type this repository manages
 *   - Long    → the type of the Entity's @Id field
 *
 * INHERITED METHODS (from JpaRepository + its parents):
 *   save(entity)           → INSERT or UPDATE (upsert based on ID presence)
 *   findById(id)           → SELECT WHERE id = ? → returns Optional<Student>
 *   findAll()              → SELECT * FROM students
 *   findAll(Pageable)      → SELECT * ... LIMIT ? OFFSET ? → returns Page<Student>
 *   deleteById(id)         → DELETE WHERE id = ?
 *   existsById(id)         → SELECT COUNT(*) WHERE id = ? → boolean
 *   count()                → SELECT COUNT(*) FROM students
 *
 * CUSTOM QUERY METHOD — Spring Data JPA parses the method name:
 *   existsByEmail(email) → generates: SELECT COUNT(*) WHERE email = ?
 *   findByEmail(email)   → generates: SELECT * WHERE email = ?
 *
 * This is called "Derived Query Methods" — no SQL needed!
 */
@Repository   // optional here (JpaRepository is already detected), but explicit is clear
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Checks if a student with the given email already exists.
     * Used in the service layer to enforce email uniqueness before saving,
     * giving a friendly error message instead of a raw DB constraint violation.
     */
    boolean existsByEmail(String email);

    /**
     * Checks email uniqueness while EXCLUDING a specific student by ID.
     * Used during UPDATE: a student should be allowed to keep their own email,
     * but not steal another student's email.
     *
     * Derived method parsing:
     *   existsBy      → SELECT COUNT(*)
     *   Email         → WHERE email = ?
     *   AndIdNot      → AND id != ?
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Fetches a student by email.
     * Returns Optional<Student> — forces the caller to handle the "not found" case.
     * Never returns null.
     */
    Optional<Student> findByEmail(String email);
}
