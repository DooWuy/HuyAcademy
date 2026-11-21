package com.example.huyacademy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "lessons")
public class Lesson extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "content_url", length = 500)
    private String contentUrl;

    @Column(name = "text_content", columnDefinition = "text")
    private String textContent;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "is_published", nullable = false)
    private boolean published;
}
