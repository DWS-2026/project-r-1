package es.codeurjc.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// We create a specific DTO to update the user, since the normal UserDTO
// does not contain the password (for security) and we need it here to update it.
public record UserUpdateDTO(
        @NotBlank(message = "The name cannot be empty")
        @Size(max = 100, message = "The name cannot exceed 100 characters")
        String name,
        
        @Size(min = 4, max = 100, message = "The password must be between 4 and 100 characters")
        String password,
        
        @Size(min = 4, max = 100, message = "The password must be between 4 and 100 characters")
        String confirmPassword
) {}