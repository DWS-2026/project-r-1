package es.codeurjc.web.repository;

import es.codeurjc.web.model.Transaction;
import es.codeurjc.web.model.User;
import es.codeurjc.web.model.Advice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByBuyer(User buyer);
    
    // FIX DoS/Logic: Prevents a user from buying the same advice repeatedly
    boolean existsByBuyerAndAdvice(User buyer, Advice advice);
}