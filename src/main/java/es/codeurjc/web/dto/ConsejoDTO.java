package es.codeurjc.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ConsejoDTO(
        Long id,
        
        @NotBlank(message = "El título es obligatorio")
        String title,
        
        @NotBlank(message = "La categoría es obligatoria")
        String category,
        
        @NotNull(message = "El precio es obligatorio")
        @Min(value = 0, message = "El precio debe ser positivo")
        Double price,
        
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @NotBlank(message = "El texto secreto no puede estar vacío")
        String secretText,
        
        // FIX: Evita que el cliente envíe su propio nombre de adjunto mediante la API
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        String attachmentName,
        
        Long sellerId,
        String sellerName
) {}