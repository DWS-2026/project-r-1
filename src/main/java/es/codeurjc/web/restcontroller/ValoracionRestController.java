package es.codeurjc.web.restcontroller;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.validation.Valid;

import es.codeurjc.web.dto.ValoracionDTO;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.model.Valoracion;
import es.codeurjc.web.service.UsuarioService;
import es.codeurjc.web.service.ValoracionService;

@RestController
@RequestMapping("/api/v1/valoraciones")
public class ValoracionRestController {

    @Autowired
    private ValoracionService valoracionService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public Page<ValoracionDTO> getAllValoraciones(Pageable pageable) {
        return valoracionService.findAll(pageable);
    }

    @GetMapping("/me")
    public ResponseEntity<List<ValoracionDTO>> getMyValoraciones(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        Usuario autor = usuarioService.findByEmail(principal.getName()).orElseThrow();
        List<ValoracionDTO> misValoraciones = valoracionService.findByAuthor(autor).stream()
                .map(valoracionService::toDTO).toList();
        return ResponseEntity.ok(misValoraciones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ValoracionDTO> getValoracion(@PathVariable Long id) {
        Valoracion valoracion = valoracionService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("La valoración con ID " + id + " no existe."));
        return ResponseEntity.ok(valoracionService.toDTO(valoracion));
    }

    @PostMapping("/")
    public ResponseEntity<Void> createValoracion(@RequestParam Long consejoId, @Valid @RequestBody ValoracionDTO valoracionDTO, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        try {
            Valoracion v = valoracionService.createReview(consejoId, principal.getName(), valoracionDTO.title(), valoracionDTO.score(), valoracionDTO.comment());
            
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(v.getId())
                    .toUri();
                    
            return ResponseEntity.created(location).build();
        } catch (Exception e) {
            throw new IllegalArgumentException("No se ha podido procesar la valoración. Verifica que el consejo exista.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateValoracion(@PathVariable Long id, @Valid @RequestBody ValoracionDTO valoracionDTO, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        boolean updated = valoracionService.updateReview(id, principal.getName(), valoracionDTO.title(), valoracionDTO.score(), valoracionDTO.comment());
        if (updated) {
            return ResponseEntity.ok().build();
        } else {
            throw new NoSuchElementException("Valoración no encontrada o no tienes permisos para editarla.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteValoracion(@PathVariable Long id, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        boolean deleted = valoracionService.deleteReview(id, principal.getName());
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            throw new NoSuchElementException("Valoración no encontrada o no tienes permisos para borrarla.");
        }
    }
}