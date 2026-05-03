package es.codeurjc.web.dto;

import java.time.LocalDateTime;

public record TransaccionDTO(
        Long id,
        
        // Evitamos meter los objetos completos Usuario y Consejo
        Long buyerId,
        String buyerName,
        
        Long consejoId,
        String consejoTitle,
        
        double priceAtPurchase,
        LocalDateTime purchaseDate
) {}