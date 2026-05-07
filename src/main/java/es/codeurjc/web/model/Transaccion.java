package es.codeurjc.web.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // The user who bought the advice
    @ManyToOne
    private Usuario buyer;

    // The advice that was purchased
    @ManyToOne
    private Consejo consejo;

    // The date and time the purchase was made
    private LocalDateTime purchaseDate;

    // Stores the price at the moment of purchase, in case the seller changes it later
    private double priceAtPurchase;

    // AHORA ES PUBLIC PARA QUE MAPSTRUCT NO FALLE
    public Transaccion() {}

    public Transaccion(Usuario buyer, Consejo consejo, double priceAtPurchase) {
        this.buyer = buyer;
        this.consejo = consejo;
        this.priceAtPurchase = priceAtPurchase;
        this.purchaseDate = LocalDateTime.now(); // Automatically sets the current time
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getBuyer() { return buyer; }
    public void setBuyer(Usuario buyer) { this.buyer = buyer; }

    public Consejo getConsejo() { return consejo; }
    public void setConsejo(Consejo consejo) { this.consejo = consejo; }

    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }

    public double getPriceAtPurchase() { return priceAtPurchase; }
    public void setPriceAtPurchase(double priceAtPurchase) { this.priceAtPurchase = priceAtPurchase; }
}