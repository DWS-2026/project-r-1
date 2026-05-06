package es.codeurjc.web.restcontroller;

import java.security.Principal;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import es.codeurjc.web.dto.UsuarioDTO;
import es.codeurjc.web.dto.UsuarioUpdateDTO;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.service.UsuarioService;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioRestController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public Page<UsuarioDTO> getAllUsuarios(Pageable pageable) {
        return usuarioService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> getUsuario(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        if (usuario.isPresent()) {
            return ResponseEntity.ok(usuarioService.toDTO(usuario.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    // Cambiado Usuario por UsuarioUpdateDTO y añadido @Valid
    public ResponseEntity<UsuarioDTO> updateUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO datosActualizados, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        Optional<Usuario> usuarioEnBBDD = usuarioService.findById(id);
        
        if (usuarioEnBBDD.isPresent() && usuarioEnBBDD.get().getEmail().equals(principal.getName())) {
            usuarioService.updateProfile(principal.getName(), datosActualizados.nombre(), datosActualizados.password(), datosActualizados.confirmPassword());
            
            // Recargamos para devolver el DTO actualizado
            Usuario actualizado = usuarioService.findByEmail(principal.getName()).get();
            return ResponseEntity.ok(usuarioService.toDTO(actualizado));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long id, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        Optional<Usuario> usuarioEnBBDD = usuarioService.findById(id);
        
        if (usuarioEnBBDD.isPresent() && usuarioEnBBDD.get().getEmail().equals(principal.getName())) {
            usuarioService.deleteUserById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}