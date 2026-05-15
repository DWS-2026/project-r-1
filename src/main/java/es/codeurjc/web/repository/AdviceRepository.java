package es.codeurjc.web.repository;

import es.codeurjc.web.model.Advice;
import es.codeurjc.web.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdviceRepository extends JpaRepository<Advice, Long> {
    List<Advice> findBySeller(User seller);
}