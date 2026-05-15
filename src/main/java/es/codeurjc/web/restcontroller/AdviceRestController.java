package es.codeurjc.web.restcontroller;

import java.net.URI;
import java.security.Principal;
import java.util.Optional;
import java.util.NoSuchElementException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.validation.Valid;

import es.codeurjc.web.dto.AdviceDTO;
import es.codeurjc.web.dto.AdviceMapper;
import es.codeurjc.web.model.Advice;
import es.codeurjc.web.model.User;
import es.codeurjc.web.service.AdviceService;
import es.codeurjc.web.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/advices")
@Tag(name = "Advices", description = "Operations related to advices put up for sale")
public class AdviceRestController {

    @Autowired
    private AdviceService adviceService;

    @Autowired
    private UserService userService;

    @Autowired
    private AdviceMapper adviceMapper;

    @Operation(summary = "Get the paginated list of advices")
    @GetMapping("/")
    public Page<AdviceDTO> getAllAdvices(Pageable pageable) {
        return adviceService.findAll(pageable);
    }

    @Operation(summary = "Get the details of a specific advice by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<AdviceDTO> getAdvice(@PathVariable Long id) {
        Advice advice = adviceService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("The advice with ID " + id + " does not exist."));
        return ResponseEntity.ok(adviceService.toDTO(advice));
    }

    @Operation(summary = "Download the cover image of an advice")
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> downloadImageRest(@PathVariable Long id) {
        Advice advice = adviceService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("The advice with ID " + id + " does not exist."));
                
        if (advice.getImageBytes() != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(advice.getImageBytes());
        }
        throw new NoSuchElementException("This advice does not have a cover image.");
    }

    @Operation(summary = "Download the extra attached file of an advice")
    @GetMapping("/{id}/attachment")
    public ResponseEntity<Resource> downloadAttachmentRest(@PathVariable Long id, HttpServletRequest request) {
        Advice advice = adviceService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("The advice with ID " + id + " does not exist."));
                
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User user = userService.findByEmail(principal.getName()).orElseThrow();
        
        // FIX: Prevent IDOR vulnerability by blocking download if you are not owner or buyer
        boolean isSeller = advice.getSeller().getId().equals(user.getId());
        boolean isAdmin = request.isUserInRole("ADMIN") || request.isUserInRole("ROLE_ADMIN");
        boolean hasBought = user.getPurchases().stream().anyMatch(t -> t.getAdvice().getId().equals(id));
        
        if (!isSeller && !isAdmin && !hasBought) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Resource file = adviceService.getAttachmentResource(id);
        if (file != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + advice.getAttachmentName() + "\"")
                    .body(file);
        }
        throw new NoSuchElementException("This advice does not have an attached file.");
    }

    @Operation(summary = "Create a new advice to put up for sale")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Advice successfully created"),
        @ApiResponse(responseCode = "401", description = "The user is not authenticated"),
        @ApiResponse(responseCode = "400", description = "Missing data in the request")
    })
    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdviceDTO> createAdvice(
            @Valid @RequestPart("advice") AdviceDTO adviceDTO,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestPart(value = "attachmentFile", required = false) MultipartFile attachmentFile,
            Principal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            Advice advice = adviceMapper.toDomain(adviceDTO);
            Advice savedAdvice = adviceService.createAdvice(advice, principal.getName(), imageFile, attachmentFile);
            AdviceDTO resultDto = adviceService.toDTO(savedAdvice);

            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(savedAdvice.getId())
                    .toUri();

            return ResponseEntity.created(location).body(resultDto);
        } catch (Exception e) {
            throw new IllegalArgumentException("Incorrect data when creating the advice: " + e.getMessage());
        }
    }

    @Operation(summary = "Update the texts of an existing advice")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdviceDTO> updateAdvice(
            @PathVariable Long id, 
            @Valid @RequestPart("advice") AdviceDTO adviceDTO,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestPart(value = "attachmentFile", required = false) MultipartFile attachmentFile,
            Principal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            Advice updatedAdviceInput = adviceMapper.toDomain(adviceDTO);
            Advice editedAdvice = adviceService.updateAdvice(id, updatedAdviceInput, principal.getName(), imageFile, attachmentFile)
                    .orElseThrow(() -> new NoSuchElementException("Advice not found or not authorized to edit it"));
            
            return ResponseEntity.ok(adviceService.toDTO(editedAdvice));
        } catch (NoSuchElementException e) {
            throw e; 
        } catch (Exception e) {
            throw new IllegalArgumentException("Error processing the image/file update.");
        }
    }

    @Operation(summary = "Remove an advice from the catalog")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdvice(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        boolean deleted = adviceService.deleteAdvice(id, principal.getName());
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            throw new NoSuchElementException("Advice not found or you don't have permission to delete it.");
        }
    }
}