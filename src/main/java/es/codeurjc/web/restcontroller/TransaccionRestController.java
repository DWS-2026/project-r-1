package es.codeurjc.web.restcontroller;

import java.net.URI;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.codeurjc.web.dto.TransaccionDTO;
import es.codeurjc.web.model.Transaccion;
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

    @PostMapping("/")
    public ResponseEntity<Void> createTransaccion(@RequestParam Long consejoId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Transaccion transaccion = transaccionService.processPayment(consejoId, principal.getName());
        if (transaccion != null) {
            // Añadida la cabecera Location que pedía la rúbrica
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(transaccion.getId())
                    .toUri();
            return ResponseEntity.created(location).build(); 
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}