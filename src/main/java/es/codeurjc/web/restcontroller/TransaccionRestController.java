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

import es.codeurjc.web.dto.TransaccionDTO;
import es.codeurjc.web.model.Transaccion;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.service.TransaccionService;
import es.codeurjc.web.service.UsuarioService;

@RestController
@RequestMapping("/api/v1/transacciones")
public class TransaccionRestController {

    @Autowired
    private TransaccionService transaccionService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public Page<TransaccionDTO> getAllTransacciones(Pageable pageable) {
        return transaccionService.findAll(pageable);
    }

    @GetMapping("/me")
    public ResponseEntity<List<TransaccionDTO>> getMyTransacciones(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        Usuario buyer = usuarioService.findByEmail(principal.getName()).orElseThrow();
        List<TransaccionDTO> misTransacciones = transaccionService.findByBuyer(buyer).stream()
                .map(transaccionService::toDTO).toList();
        return ResponseEntity.ok(misTransacciones);
    }

    @PostMapping("/")
    public ResponseEntity<Void> createTransaccion(@RequestParam Long consejoId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Transaccion transaccion = transaccionService.processPayment(consejoId, principal.getName());
        if (transaccion != null) {
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(transaccion.getId())
                    .toUri();
            return ResponseEntity.created(location).build(); 
        } else {
            // Este error será atrapado por el RestValidationExceptionHandler mostrando un 400 en JSON
            throw new IllegalArgumentException("Transacción inválida. Verifica que no estés comprando tu propio consejo.");
        }
    }
}