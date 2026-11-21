package com.example.huyacademy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "enrollment_date", nullable = false, updatable = false)
    private Instant enrollmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EnrollmentStatus status = EnrollmentStatus.ENROLLED;

    @Column(name = "completion_date")
    private Instant completionDate;

    @Column(name = "progress_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal progressPercentage = BigDecimal.ZERO;

    @PrePersist
    void initializeEnrollmentDate() {
        if (enrollmentDate == null) {
            enrollmentDate = Instant.now();
        }
    }
}
