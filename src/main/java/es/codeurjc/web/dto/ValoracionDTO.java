package es.codeurjc.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ValoracionDTO(
        Long id,
        
        Long authorId,
        String authorName,
        
        Long consejoId,
        String consejoTitle,
        
        @Min(value = 0, message = "La nota mínima es 0")
        @Max(value = 10, message = "La nota máxima es 10")
        int score,
        
        @NotBlank(message = "El título de la valoración es obligatorio")
        String title,
        
        @NotBlank(message = "El comentario es obligatorio")
        String comment
) {}