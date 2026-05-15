package es.codeurjc.web.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
public class Advice {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;
    
    private String category;
    
    private double price;

    @Column(columnDefinition = "TEXT")
    private String secretText;

    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] imageBytes;

    // --- NEW FIELDS FOR THE FILE ON DISK ---
    private String attachmentName; // Original name to show the user
    private String attachmentPath; // Safe internal path on the server

    @ManyToOne
    private User seller;

    @OneToMany(mappedBy = "advice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "advice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    // NOW PUBLIC SO MAPSTRUCT DOES NOT FAIL
    public Advice() {}

    public Advice(String title, String category, double price, String secretText, User seller) {
        this.title = title;
        this.category = category;
        this.price = price;
        this.secretText = secretText;
        this.seller = seller;
    }

    public Advice(String title, String category, double price, String secretText, String imagePath, User seller) {
        this.title = title;
        this.category = category;
        this.price = price;
        this.secretText = secretText;
        this.imageBytes = null;
        this.seller = seller;
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getSecretText() { return secretText; }
    public void setSecretText(String secretText) { this.secretText = secretText; }

    public byte[] getImageBytes() { return imageBytes; }
    public void setImageBytes(byte[] imageBytes) { this.imageBytes = imageBytes; }

    public String getAttachmentName() { return attachmentName; }
    public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }

    public String getAttachmentPath() { return attachmentPath; }
    public void setAttachmentPath(String attachmentPath) { this.attachmentPath = attachmentPath; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
}