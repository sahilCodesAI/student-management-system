package com.example.studentmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * JPA Entity — maps directly to the 'students' table in MySQL.
 *
 * Layer role: entity/ holds all database-mapped classes.
 * Entities must NEVER be sent over the API directly (use DTOs for that).
 */
@Entity
@Table(
    name = "students",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_email", columnNames = "email")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    // ── Primary Key ──────────────────────────────────────────────────────────
    // @Id            → marks this field as the primary key
    // @GeneratedValue(IDENTITY) → delegates to MySQL AUTO_INCREMENT
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Columns ──────────────────────────────────────────────────────────────
    // name       → snake_case column name (DB convention)
    // nullable   → false means NOT NULL constraint at the DB level
    // length     → controls VARCHAR size (default would be 255)

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    // Unique constraint is defined at @Table level above with a readable name.
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    // Phone is a String — not Integer — because:
    //   • Can start with 0 (leading zeros lost in int)
    //   • May contain +, -, (, ) characters
    @Column(name = "phone", length = 20)
    private String phone;

    // Integer (wrapper) not int (primitive) — allows null for optional field.
    // int defaults to 0 if null, which is semantically wrong.
    @Column(name = "age")
    private Integer age;

    @Column(name = "course", length = 150)
    private String course;
}
