package es.codeurjc.web.repository;

import es.codeurjc.web.model.Consejo;
import es.codeurjc.web.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConsejoRepository extends JpaRepository<Consejo, Long> {
    List<Consejo> findBySeller(Usuario seller);
}