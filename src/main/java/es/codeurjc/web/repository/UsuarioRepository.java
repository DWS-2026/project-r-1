package es.codeurjc.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import es.codeurjc.web.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // You do not need to write any code here right now.
    // JpaRepository already includes the save() method internally.
}