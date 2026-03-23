package es.codeurjc.web.repository;

import es.codeurjc.web.model.Transaccion;
import es.codeurjc.web.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
    List<Transaccion> findByBuyer(Usuario buyer);
}