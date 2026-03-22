package es.codeurjc.web.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
public class Consejo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;
    
    private String category;
    
    private double price;

    @Column(columnDefinition = "TEXT")
    private String secretText;

// ELIMINA la variable imagePath y pon esto en su lugar:
    
    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] imageBytes;

    // --- Asegúrate de actualizar el constructor para quitar imagePath ---
    public Consejo(String title, String category, double price, String secretText, Usuario seller) {
        this.title = title;
        this.category = category;
        this.price = price;
        this.secretText = secretText;
        this.seller = seller;
    }

    // --- Getters y Setters de la imagen ---
    public byte[] getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }
    // --- Relationships ---

    // Many advices can be sold by one user
    @ManyToOne
    private Usuario seller;

    // One advice can have multiple transactions (purchases)
    @OneToMany(mappedBy = "consejo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaccion> transactions = new ArrayList<>();

    // One advice can have multiple reviews
    @OneToMany(mappedBy = "consejo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Valoracion> reviews = new ArrayList<>();

    // Empty constructor required by JPA
    protected Consejo() {}

    // Parameterized constructor
    public Consejo(String title, String category, double price, String secretText, String imagePath, Usuario seller) {
        this.title = title;
        this.category = category;
        this.price = price;
        this.secretText = secretText;
        this.imageBytes = null;
        this.seller = seller;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getSecretText() {
        return secretText;
    }

    public void setSecretText(String secretText) {
        this.secretText = secretText;
    }


    public Usuario getSeller() {
        return seller;
    }

    public void setSeller(Usuario seller) {
        this.seller = seller;
    }

    public List<Transaccion> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaccion> transactions) {
        this.transactions = transactions;
    }

    public List<Valoracion> getReviews() {
        return reviews;
    }

    public void setReviews(List<Valoracion> reviews) {
        this.reviews = reviews;
    }
}