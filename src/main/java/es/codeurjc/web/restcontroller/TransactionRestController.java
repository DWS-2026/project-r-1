package es.codeurjc.web.restcontroller;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.codeurjc.web.dto.TransactionDTO;
import es.codeurjc.web.model.Transaction;
import es.codeurjc.web.model.User;
import es.codeurjc.web.service.TransactionService;
import es.codeurjc.web.service.UserService;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionRestController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public Page<TransactionDTO> getAllTransactions(Pageable pageable) {
        return transactionService.findAll(pageable);
    }

    @GetMapping("/me")
    public ResponseEntity<List<TransactionDTO>> getMyTransactions(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        User buyer = userService.findByEmail(principal.getName()).orElseThrow();
        List<TransactionDTO> myTransactions = transactionService.findByBuyer(buyer).stream()
                .map(transactionService::toDTO).toList();
        return ResponseEntity.ok(myTransactions);
    }

    @PostMapping("/")
    public ResponseEntity<Void> createTransaction(@RequestParam Long adviceId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Transaction transaction = transactionService.processPayment(adviceId, principal.getName());
        if (transaction != null) {
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(transaction.getId())
                    .toUri();
            return ResponseEntity.created(location).build(); 
        } else {
            // This error will be caught by RestValidationExceptionHandler showing a JSON 400
            throw new IllegalArgumentException("Invalid transaction. Check that you are not buying your own advice.");
        }
    }
}