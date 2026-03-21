package es.codeurjc.web.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import es.codeurjc.web.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Spring Data JPA will automatically implement this method based on the method name
    Optional<Usuario> findByEmail(String email);
}