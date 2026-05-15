package es.codeurjc.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Creamos un DTO específico para actualizar el usuario, ya que el UsuarioDTO normal
// no contiene la contraseña (por seguridad) y la necesitamos aquí para actualizarla.
public record UsuarioUpdateDTO(
        @NotBlank(message = "El nombre no puede estar vacío")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,
        
        @Size(min = 4, max = 100, message = "La contraseña debe tener entre 4 y 100 caracteres")
        String password,
        
        @Size(min = 4, max = 100, message = "La contraseña debe tener entre 4 y 100 caracteres")
        String confirmPassword
) {}