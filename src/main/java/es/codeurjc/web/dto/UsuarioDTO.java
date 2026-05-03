package es.codeurjc.web.dto;

import java.util.List;

// Fíjate que NO incluimos la contraseña. Así garantizamos que nunca se escape por la API.
public record UsuarioDTO(
        Long id,
        String nombre,
        String email,
        List<String> roles
) {}