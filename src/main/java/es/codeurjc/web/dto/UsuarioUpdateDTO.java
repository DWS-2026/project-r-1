package es.codeurjc.web.dto;

import jakarta.validation.constraints.NotBlank;

// Creamos un DTO específico para actualizar el usuario, ya que el UsuarioDTO normal
// no contiene la contraseña (por seguridad) y la necesitamos aquí para actualizarla.
public record UsuarioUpdateDTO(
        @NotBlank(message = "El nombre no puede estar vacío")
        String nombre,
        
        String password,
        String confirmPassword
) {}