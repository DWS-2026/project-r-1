package es.codeurjc.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ConsejoDTO(
        Long id,
        
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar los 200 caracteres")
        String title,
        
        @NotBlank(message = "La categoría es obligatoria")
        @Size(max = 100, message = "La categoría no puede superar los 100 caracteres")
        String category,
        
        @NotNull(message = "El precio es obligatorio")
        @Min(value = 0, message = "El precio debe ser positivo")
        Double price,
        
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @NotBlank(message = "El texto secreto no puede estar vacío")
        @Size(max = 5000, message = "El texto secreto no puede superar los 5000 caracteres")
        String secretText,
        
        // FIX: Evita que el cliente envíe su propio nombre de adjunto mediante la API
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        String attachmentName,
        
        Long sellerId,
        String sellerName
) {}