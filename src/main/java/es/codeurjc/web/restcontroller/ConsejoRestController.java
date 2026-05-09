package es.codeurjc.web.restcontroller;

import java.net.URI;
import java.security.Principal;
import java.util.Optional;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.validation.Valid;

import es.codeurjc.web.dto.ConsejoDTO;
import es.codeurjc.web.dto.ConsejoMapper;
import es.codeurjc.web.model.Consejo;
import es.codeurjc.web.service.ConsejoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/consejos")
@Tag(name = "Consejos", description = "Operaciones relacionadas con los consejos puestos a la venta")
public class ConsejoRestController {

    @Autowired
    private ConsejoService consejoService;

    @Autowired
    private ConsejoMapper consejoMapper;

    @Operation(summary = "Obtener el listado de consejos paginados")
    @GetMapping("/")
    public Page<ConsejoDTO> getAllConsejos(Pageable pageable) {
        return consejoService.findAll(pageable);
    }

    @Operation(summary = "Obtener los detalles de un consejo específico por su ID")
    @GetMapping("/{id}")
    public ResponseEntity<ConsejoDTO> getConsejo(@PathVariable Long id) {
        Consejo consejo = consejoService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("El consejo con ID " + id + " no existe."));
        return ResponseEntity.ok(consejoService.toDTO(consejo));
    }

    @Operation(summary = "Descargar la imagen de portada de un consejo")
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> downloadImageRest(@PathVariable Long id) {
        Consejo consejo = consejoService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("El consejo con ID " + id + " no existe."));
                
        if (consejo.getImageBytes() != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(consejo.getImageBytes());
        }
        throw new NoSuchElementException("Este consejo no tiene imagen de portada.");
    }

    @Operation(summary = "Descargar el fichero adjunto extra de un consejo")
    @GetMapping("/{id}/attachment")
    public ResponseEntity<Resource> downloadAttachmentRest(@PathVariable Long id) {
        Consejo consejo = consejoService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("El consejo con ID " + id + " no existe."));
                
        Resource file = consejoService.getAttachmentResource(id);
        if (file != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + consejo.getAttachmentName() + "\"")
                    .body(file);
        }
        throw new NoSuchElementException("Este consejo no tiene archivo adjunto.");
    }

    @Operation(summary = "Crear un nuevo consejo para poner a la venta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Consejo creado exitosamente"),
        @ApiResponse(responseCode = "401", description = "El usuario no está autenticado"),
        @ApiResponse(responseCode = "400", description = "Faltan datos en la petición")
    })
    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ConsejoDTO> createConsejo(
            @Valid @RequestPart("consejo") ConsejoDTO consejoDTO,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestPart(value = "attachmentFile", required = false) MultipartFile attachmentFile,
            Principal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            Consejo consejo = consejoMapper.toDomain(consejoDTO);
            Consejo savedConsejo = consejoService.createAdvice(consejo, principal.getName(), imageFile, attachmentFile);
            ConsejoDTO resultDto = consejoService.toDTO(savedConsejo);

            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(savedConsejo.getId())
                    .toUri();

            return ResponseEntity.created(location).body(resultDto);
        } catch (Exception e) {
            throw new IllegalArgumentException("Datos incorrectos al crear el consejo: " + e.getMessage());
        }
    }

    @Operation(summary = "Actualizar los textos de un consejo existente")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ConsejoDTO> updateConsejo(
            @PathVariable Long id, 
            @Valid @RequestPart("consejo") ConsejoDTO consejoDTO,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestPart(value = "attachmentFile", required = false) MultipartFile attachmentFile,
            Principal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            Consejo consejoActualizado = consejoMapper.toDomain(consejoDTO);
            Consejo consejoEditado = consejoService.updateAdvice(id, consejoActualizado, principal.getName(), imageFile, attachmentFile)
                    .orElseThrow(() -> new NoSuchElementException("Consejo no encontrado o no autorizado a editarlo"));
            
            return ResponseEntity.ok(consejoService.toDTO(consejoEditado));
        } catch (NoSuchElementException e) {
            throw e; 
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al procesar la actualización de la imagen/archivo.");
        }
    }

    @Operation(summary = "Eliminar un consejo del catálogo")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConsejo(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        boolean deleted = consejoService.deleteAdvice(id, principal.getName());
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            throw new NoSuchElementException("Consejo no encontrado o no tienes permisos para borrarlo.");
        }
    }
}