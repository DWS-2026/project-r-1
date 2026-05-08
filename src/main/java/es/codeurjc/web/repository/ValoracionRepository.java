package es.codeurjc.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import es.codeurjc.web.model.Valoracion;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.model.Consejo;
import java.util.List;
import java.util.Optional;

public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {
    // Busca si este autor ya tiene una review para este consejo
    Optional<Valoracion> findByAuthorAndConsejo(Usuario author, Consejo consejo);
    
    // Lista todas las valoraciones hechas por un usuario en concreto
    List<Valoracion> findByAuthor(Usuario author);
}