package com.sms.student;

import jakarta.persistence.*;
import lombok.*;

/**
 * ══════════════════════════════════════════════════════
 *  STUDENT JPA ENTITY
 * ══════════════════════════════════════════════════════
 *
 * A JPA Entity is a plain Java class (POJO) that Hibernate
 * maps to a relational database table.
 *
 * Hibernate reads the annotations below at startup and:
 *   1. Creates (or validates) the 'students' table in MySQL
 *   2. Knows which Java field maps to which DB column
 *   3. Handles SQL generation for INSERT / SELECT / UPDATE / DELETE
 *
 * ──────────────────────────────────────────────────────
 * @Entity
 * ──────────────────────────────────────────────────────
 * Marks this class as a JPA-managed entity.
 * Without this, Hibernate ignores the class entirely.
 * Every entity MUST have a no-argument constructor (provided by Lombok below).
 */
@Entity

/**
 * ──────────────────────────────────────────────────────
 * @Table(name = "students")
 * ──────────────────────────────────────────────────────
 * Tells Hibernate the exact name of the database table to use.
 *
 * Without @Table, Hibernate uses the class name as the table name → "Student".
 * We override it to "students" to follow DB naming conventions (plural, lowercase).
 *
 * uniqueConstraints → creates a named UNIQUE INDEX on the 'email' column.
 * The name "uk_student_email" makes the constraint easy to identify
 * if it shows up in a DB error or an ALTER TABLE statement.
 *
 * DIFFERENCE between @Table uniqueConstraints vs @Column(unique=true):
 *   @Column(unique=true)          → creates an anonymous unique index on that column
 *   @Table(uniqueConstraints=...) → creates a NAMED unique index (better for debugging)
 * Both achieve the same DB result; the named approach is preferred.
 */
@Table(
    name = "students",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_email", columnNames = "email")
    }
)

/**
 * ──────────────────────────────────────────────────────
 * Lombok Annotations
 * ──────────────────────────────────────────────────────
 * These are processed at compile time by the Lombok annotation processor.
 * They generate Java code so you don't have to write boilerplate manually.
 *
 * @Getter          → generates public getFirstName(), getEmail(), etc. for every field
 * @Setter          → generates public setFirstName(String v), etc. for every field
 * @NoArgsConstructor → generates public Student() {}
 *                     JPA REQUIRES a no-arg constructor to instantiate entities
 *                     via reflection when loading rows from the DB.
 * @AllArgsConstructor → generates public Student(Long id, String firstName, ...)
 *                      useful in tests and with @Builder internally
 * @Builder         → enables the fluent builder pattern:
 *                      Student s = Student.builder()
 *                                         .firstName("Ali")
 *                                         .email("ali@test.com")
 *                                         .build();
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    /**
     * ──────────────────────────────────────────────────────
     * @Id
     * ──────────────────────────────────────────────────────
     * Marks this field as the PRIMARY KEY of the table.
     * Every JPA entity must have exactly one @Id field.
     *
     * We use Long (wrapper type) instead of long (primitive) because:
     *   - Long can be null (before the entity is persisted, id = null)
     *   - Spring Data JPA requires the ID type to be Serializable
     *   - Long supports up to 9,223,372,036,854,775,807 — plenty of IDs
     */
    @Id

    /**
     * ──────────────────────────────────────────────────────
     * @GeneratedValue(strategy = GenerationType.IDENTITY)
     * ──────────────────────────────────────────────────────
     * Tells Hibernate NOT to set the ID manually — let the database generate it.
     *
     * GenerationType.IDENTITY:
     *   → Delegates ID generation to MySQL's AUTO_INCREMENT column.
     *   → After INSERT, Hibernate reads back the generated ID with LAST_INSERT_ID().
     *   → Best strategy for MySQL because it's simple and efficient.
     *
     * Other strategies (for reference):
     *   SEQUENCE  → uses a DB sequence object (PostgreSQL, Oracle)
     *   TABLE     → uses a special keeper table for IDs (slow, avoid)
     *   AUTO      → lets Hibernate pick — unpredictable, avoid in production
     */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ──────────────────────────────────────────────────────
     * @Column
     * ──────────────────────────────────────────────────────
     * Maps this field to a specific database column with constraints.
     *
     * name       = "first_name"
     *   → The column will be named 'first_name' in MySQL (snake_case convention).
     *   → Without this, Hibernate uses the field name as-is: "firstName" (camelCase).
     *   → Databases prefer snake_case; Java prefers camelCase — @Column bridges them.
     *
     * nullable   = false
     *   → Adds NOT NULL constraint at the database level.
     *   → Hibernate also skips null-checks in dirty tracking for performance.
     *
     * length     = 100
     *   → Generates VARCHAR(100) instead of the default VARCHAR(255).
     *   → Only affects String fields. Ignored for Integer, Long, etc.
     */
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    /**
     * Email — must be unique across all students.
     *
     * nullable = false → email is required
     * length = 255     → standard email max length per RFC 5321
     *
     * The UNIQUE enforcement comes from the @Table uniqueConstraints above.
     * If a duplicate email is inserted, MySQL throws a constraint violation
     * which surfaces as a DataIntegrityViolationException in Spring.
     * Our service layer pre-checks with existsByEmail() to give a friendlier error.
     */
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    /**
     * Phone number stored as a String, NOT an integer/long.
     *
     * WHY String for phone?
     *   - Phone numbers can start with 0 (e.g. "07911123456") — integers drop leading zeros
     *   - They may contain formatting characters: +, -, (, ), spaces
     *   - They are identifiers, not values to do arithmetic on
     *   - International formats vary: "+44 20 7946 0958" is valid
     *
     * nullable = true (default) → phone is optional.
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * Age stored as Integer (wrapper type, not int primitive).
     *
     * WHY Integer over int?
     *   - int cannot be null; Integer can be null (age is optional here)
     *   - JPA maps null Integer → NULL in DB, which is correct
     *   - If you used int and the column is nullable, Hibernate would store 0
     *     instead of NULL — which is semantically wrong
     *
     * In a real system you'd calculate age from dateOfBirth at runtime,
     * but storing it as a field is fine for a learning project.
     */
    @Column(name = "age")
    private Integer age;

    /**
     * Course the student is enrolled in.
     * Stored as a plain String (e.g. "Computer Science", "Mathematics").
     *
     * In a more advanced system, this would be a @ManyToOne relationship
     * to a separate Course entity. For now, a String keeps things simple.
     */
    @Column(name = "course", length = 150)
    private String course;
}
