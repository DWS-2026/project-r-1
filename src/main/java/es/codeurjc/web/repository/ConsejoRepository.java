package es.codeurjc.web.repository;

import es.codeurjc.web.model.Consejo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsejoRepository extends JpaRepository<Consejo, Long> {
    // Spring Data JPA implements standard methods automatically
}