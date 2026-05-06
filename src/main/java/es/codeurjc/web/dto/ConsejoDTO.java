package es.codeurjc.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConsejoDTO(
        Long id,
        
        @NotBlank(message = "El título es obligatorio")
        String title,
        
        @NotBlank(message = "La categoría es obligatoria")
        String category,
        
        @NotNull(message = "El precio es obligatorio")
        @Min(value = 0, message = "El precio debe ser positivo")
        Double price,
        
        @NotBlank(message = "El texto secreto no puede estar vacío")
        String secretText,
        
        String attachmentName,
        Long sellerId,
        String sellerName
) {}