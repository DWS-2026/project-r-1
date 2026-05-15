package es.codeurjc.web.model;

import jakarta.persistence.*;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // The user who wrote the review
    @ManyToOne
    private User author;

    // The advice being reviewed
    @ManyToOne
    private Advice advice;

    // Rating score (e.g., from 1 to 5 stars)
    private int score;

    private String title;

    // Update constructor to include title:
    public Review(User author, Advice advice, int score, String title, String comment) {
        this.author = author;
        this.advice = advice;
        this.score = score;
        this.title = title;
        this.comment = comment;
    }

    // Add getters and setters for title:
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    // Text of the review
    @Column(columnDefinition = "TEXT")
    private String comment;

    // NOW PUBLIC SO MAPSTRUCT DOES NOT FAIL
    public Review() {}

    public Review(User author, Advice advice, int score, String comment) {
        this.author = author;
        this.advice = advice;
        this.score = score;
        this.comment = comment;
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public Advice getAdvice() { return advice; }
    public void setAdvice(Advice advice) { this.advice = advice; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}