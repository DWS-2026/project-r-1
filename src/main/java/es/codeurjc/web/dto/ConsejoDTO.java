package es.codeurjc.web.dto;

public record ConsejoDTO(
        Long id,
        String title,
        String category,
        double price,
        String secretText,
        
        // Exponemos el nombre original del archivo para la API
        String attachmentName,
        
        Long sellerId,
        String sellerName
) {}