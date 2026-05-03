package es.codeurjc.web.restcontroller;

import java.net.URI;
import java.security.Principal;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.codeurjc.web.dto.ConsejoDTO;
import es.codeurjc.web.model.Consejo;
import es.codeurjc.web.service.ConsejoService;

@RestController
@RequestMapping("/api/v1/consejos")
public class ConsejoRestController {

    @Autowired
    private ConsejoService consejoService;

    @GetMapping("/")
    public Page<ConsejoDTO> getAllConsejos(Pageable pageable) {
        return consejoService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsejoDTO> getConsejo(@PathVariable Long id) {
        Optional<Consejo> consejo = consejoService.findById(id);
        if (consejo.isPresent()) {
            return ResponseEntity.ok(consejoService.toDTO(consejo.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint en la API REST para descargar el fichero adjunto
    @GetMapping("/{id}/attachment")
    public ResponseEntity<Resource> downloadAttachmentRest(@PathVariable Long id) {
        Resource file = consejoService.getAttachmentResource(id);
        Optional<Consejo> consejo = consejoService.findById(id);
        
        if (file != null && consejo.isPresent()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + consejo.get().getAttachmentName() + "\"")
                    .body(file);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/")
    public ResponseEntity<ConsejoDTO> createConsejo(@RequestBody Consejo consejo, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            Consejo savedConsejo = consejoService.createAdvice(consejo, principal.getName(), null, null);
            ConsejoDTO dto = consejoService.toDTO(savedConsejo);

            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(savedConsejo.getId())
                    .toUri();

            return ResponseEntity.created(location).body(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsejoDTO> updateConsejo(@PathVariable Long id, @RequestBody Consejo consejoActualizado, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            Optional<Consejo> consejoEditado = consejoService.updateAdvice(id, consejoActualizado, principal.getName(), null, null);
            if (consejoEditado.isPresent()) {
                return ResponseEntity.ok(consejoService.toDTO(consejoEditado.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConsejo(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        boolean deleted = consejoService.deleteAdvice(id, principal.getName());
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}