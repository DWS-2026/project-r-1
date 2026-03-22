package es.codeurjc.web.repository;

import es.codeurjc.web.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
}