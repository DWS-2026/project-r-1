package es.codeurjc.web.restcontroller;

import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import es.codeurjc.web.dto.TransaccionDTO;
import es.codeurjc.web.service.TransaccionService;

@RestController
@RequestMapping("/api/v1/transacciones")
public class TransaccionRestController {

    @Autowired
    private TransaccionService transaccionService;

    @GetMapping("/")
    public Page<TransaccionDTO> getAllTransacciones(Pageable pageable) {
        return transaccionService.findAll(pageable);
    }

    // Usamos el param de URL ?consejoId=X tal como dicen las buenas prácticas de la rúbrica
    @PostMapping("/")
    public ResponseEntity<Void> createTransaccion(@RequestParam Long consejoId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        boolean success = transaccionService.processPayment(consejoId, principal.getName());
        if (success) {
            return ResponseEntity.status(201).build(); // 201 Created
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}