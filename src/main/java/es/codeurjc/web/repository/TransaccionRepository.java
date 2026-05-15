package es.codeurjc.web.repository;

import es.codeurjc.web.model.Transaccion;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.model.Consejo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
    List<Transaccion> findByBuyer(Usuario buyer);
    
    // FIX DoS/Logica: Impide que un usuario compre el mismo consejo repetidamente
    boolean existsByBuyerAndConsejo(Usuario buyer, Consejo consejo);
}