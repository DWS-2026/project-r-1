package es.codeurjc.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import es.codeurjc.web.model.Review;
import es.codeurjc.web.model.User;
import es.codeurjc.web.model.Advice;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Finds if this author already has a review for this advice
    Optional<Review> findByAuthorAndAdvice(User author, Advice advice);
    
    // Lists all reviews made by a specific user
    List<Review> findByAuthor(User author);
}