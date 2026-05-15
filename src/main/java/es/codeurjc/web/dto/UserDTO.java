package es.codeurjc.web.dto;

import java.util.List;

// Note that we DO NOT include the password. This guarantees it never leaks through the API.
public record UserDTO(
        Long id,
        String name,
        String email,
        List<String> roles
) {}