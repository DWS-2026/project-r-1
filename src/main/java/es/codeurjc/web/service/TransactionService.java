package es.codeurjc.web.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import es.codeurjc.web.model.Advice;
import es.codeurjc.web.model.Transaction;
import es.codeurjc.web.model.User;
import es.codeurjc.web.repository.TransactionRepository;
import es.codeurjc.web.dto.TransactionDTO;
import es.codeurjc.web.dto.TransactionMapper;

@Service
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AdviceService adviceService;

    @Autowired
    private TransactionMapper transactionMapper;

    public TransactionDTO toDTO(Transaction t) {
        return transactionMapper.toDTO(t);
    }

    public void save(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    public List<Transaction> findByBuyer(User buyer) {
        return transactionRepository.findByBuyer(buyer);
    }

    public Page<TransactionDTO> findAll(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(transactionMapper::toDTO);
    }

    // Changed from boolean to object to be able to retrieve the ID and create the Location header in REST
    public Transaction processPayment(Long adviceId, String buyerEmail) {
        User buyer = userService.findByEmail(buyerEmail).orElseThrow();
        Advice advice = adviceService.findById(adviceId).orElseThrow();

        if (advice.getSeller() != null && advice.getSeller().getId().equals(buyer.getId())) {
            return null; // The user cannot buy their own advice
        }

        // FIX DoS/Logic: Prevent duplicate purchases that saturate the database
        if (transactionRepository.existsByBuyerAndAdvice(buyer, advice)) {
            throw new IllegalArgumentException("You have already acquired this advice. It is not necessary to buy it again.");
        }

        Transaction transaction = new Transaction(buyer, advice, advice.getPrice());
        return transactionRepository.save(transaction);
    }
}