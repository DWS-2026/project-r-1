package es.codeurjc.web.model;

import jakarta.persistence.*;

@Entity
public class Valoracion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // The user who wrote the review
    @ManyToOne
    private Usuario author;

    // The advice being reviewed
    @ManyToOne
    private Consejo consejo;

    // Rating score (e.g., from 1 to 5 stars)
    private int score;

    // Text of the review
    @Column(columnDefinition = "TEXT")
    private String comment;

    // Empty constructor required by JPA
    protected Valoracion() {}

    public Valoracion(Usuario author, Consejo consejo, int score, String comment) {
        this.author = author;
        this.consejo = consejo;
        this.score = score;
        this.comment = comment;
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getAuthor() { return author; }
    public void setAuthor(Usuario author) { this.author = author; }

    public Consejo getConsejo() { return consejo; }
    public void setConsejo(Consejo consejo) { this.consejo = consejo; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}