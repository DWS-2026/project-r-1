package es.codeurjc.web.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // The user who bought the advice
    @ManyToOne
    private User buyer;

    // The advice that was purchased
    @ManyToOne
    private Advice advice;

    // The date and time the purchase was made
    private LocalDateTime purchaseDate;

    // Stores the price at the moment of purchase, in case the seller changes it later
    private double priceAtPurchase;

    // NOW PUBLIC SO MAPSTRUCT DOES NOT FAIL
    public Transaction() {}

    public Transaction(User buyer, Advice advice, double priceAtPurchase) {
        this.buyer = buyer;
        this.advice = advice;
        this.priceAtPurchase = priceAtPurchase;
        this.purchaseDate = LocalDateTime.now(); // Automatically sets the current time
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }

    public Advice getAdvice() { return advice; }
    public void setAdvice(Advice advice) { this.advice = advice; }

    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }

    public double getPriceAtPurchase() { return priceAtPurchase; }
    public void setPriceAtPurchase(double priceAtPurchase) { this.priceAtPurchase = priceAtPurchase; }
}