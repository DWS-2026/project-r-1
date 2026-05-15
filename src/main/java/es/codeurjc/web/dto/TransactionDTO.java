package es.codeurjc.web.dto;

import java.time.LocalDateTime;

public record TransactionDTO(
        Long id,
        
        // We avoid putting complete User and Advice objects
        Long buyerId,
        String buyerName,
        
        Long adviceId,
        String adviceTitle,
        
        double priceAtPurchase,
        LocalDateTime purchaseDate
) {}