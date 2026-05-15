package es.codeurjc.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AdviceDTO(
        Long id,
        
        @NotBlank(message = "The title is mandatory")
        @Size(max = 200, message = "The title cannot exceed 200 characters")
        String title,
        
        @NotBlank(message = "The category is mandatory")
        @Size(max = 100, message = "The category cannot exceed 100 characters")
        String category,
        
        @NotNull(message = "The price is mandatory")
        @Min(value = 0, message = "The price must be positive")
        Double price,
        
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @NotBlank(message = "The secret text cannot be empty")
        @Size(max = 5000, message = "The secret text cannot exceed 5000 characters")
        String secretText,
        
        // FIX: Prevents the client from sending their own attachment name via the API
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        String attachmentName,
        
        Long sellerId,
        String sellerName
) {}