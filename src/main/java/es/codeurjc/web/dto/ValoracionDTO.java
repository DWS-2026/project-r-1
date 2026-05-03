package es.codeurjc.web.dto;

public record ValoracionDTO(
        Long id,
        
        // Evitamos meter los objetos completos Usuario y Consejo
        Long authorId,
        String authorName,
        
        Long consejoId,
        String consejoTitle,
        
        int score,
        String title,
        String comment
) {}