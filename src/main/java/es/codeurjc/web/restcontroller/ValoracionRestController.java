package es.codeurjc.web.restcontroller;

import java.security.Principal;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import es.codeurjc.web.dto.ValoracionDTO;
import es.codeurjc.web.model.Valoracion;
import es.codeurjc.web.service.ValoracionService;

@RestController
@RequestMapping("/api/v1/valoraciones")
public class ValoracionRestController {

    @Autowired
    private ValoracionService valoracionService;

    @GetMapping("/")
    public Page<ValoracionDTO> getAllValoraciones(Pageable pageable) {
        return valoracionService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ValoracionDTO> getValoracion(@PathVariable Long id) {
        Optional<Valoracion> valoracion = valoracionService.findById(id);
        if (valoracion.isPresent()) {
            return ResponseEntity.ok(valoracionService.toDTO(valoracion.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/")
    public ResponseEntity<Void> createValoracion(@RequestParam Long consejoId, @RequestBody Valoracion valoracion, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        try {
            valoracionService.createReview(consejoId, principal.getName(), valoracion.getTitle(), valoracion.getScore(), valoracion.getComment());
            return ResponseEntity.status(201).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateValoracion(@PathVariable Long id, @RequestBody Valoracion valoracion, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        boolean updated = valoracionService.updateReview(id, principal.getName(), valoracion.getTitle(), valoracion.getScore(), valoracion.getComment());
        if (updated) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteValoracion(@PathVariable Long id, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        boolean deleted = valoracionService.deleteReview(id, principal.getName());
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}