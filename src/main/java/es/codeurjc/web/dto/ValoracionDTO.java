package es.codeurjc.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
        @Size(max = 200, message = "El título no puede superar los 200 caracteres")
        String title,
        
        @NotBlank(message = "El comentario es obligatorio")
        @Size(max = 2000, message = "El comentario no puede superar los 2000 caracteres")
        String comment
) {}