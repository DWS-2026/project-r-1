package es.codeurjc.web.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import es.codeurjc.web.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA will automatically implement this method based on the method name
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);

}