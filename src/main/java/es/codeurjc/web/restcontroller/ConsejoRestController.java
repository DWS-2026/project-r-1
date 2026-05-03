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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/consejos")
// Añadimos etiquetas para organizar visualmente la documentación HTML
@Tag(name = "Consejos", description = "Operaciones relacionadas con los consejos puestos a la venta")
public class ConsejoRestController {

    @Autowired
    private ConsejoService consejoService;

    @Operation(summary = "Obtener el listado de consejos paginados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping("/")
    public Page<ConsejoDTO> getAllConsejos(Pageable pageable) {
        return consejoService.findAll(pageable);
    }

    @Operation(summary = "Obtener los detalles de un consejo específico por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Consejo encontrado"),
        @ApiResponse(responseCode = "404", description = "No existe un consejo con el ID proporcionado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ConsejoDTO> getConsejo(@PathVariable Long id) {
        Optional<Consejo> consejo = consejoService.findById(id);
        if (consejo.isPresent()) {
            return ResponseEntity.ok(consejoService.toDTO(consejo.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Descargar el fichero adjunto extra de un consejo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fichero descargado correctamente"),
        @ApiResponse(responseCode = "404", description = "Fichero o consejo no encontrado")
    })
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

    @Operation(summary = "Crear un nuevo consejo para poner a la venta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Consejo creado exitosamente"),
        @ApiResponse(responseCode = "401", description = "El usuario no está autenticado"),
        @ApiResponse(responseCode = "400", description = "Faltan datos en la petición")
    })
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

    @Operation(summary = "Actualizar los textos de un consejo existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Consejo actualizado"),
        @ApiResponse(responseCode = "401", description = "El usuario no está autenticado"),
        @ApiResponse(responseCode = "404", description = "El consejo no existe o el usuario no es el dueño")
    })
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

    @Operation(summary = "Eliminar un consejo del catálogo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Consejo borrado exitosamente"),
        @ApiResponse(responseCode = "401", description = "El usuario no está autenticado"),
        @ApiResponse(responseCode = "404", description = "El consejo no existe o el usuario no es el dueño")
    })
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