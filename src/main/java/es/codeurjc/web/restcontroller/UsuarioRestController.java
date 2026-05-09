package es.codeurjc.web.restcontroller;

import java.security.Principal;
import java.util.Optional;
import java.util.NoSuchElementException;
import jakarta.servlet.http.HttpServletRequest;
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
        Usuario usuario = usuarioService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("El usuario con ID " + id + " no existe."));
        return ResponseEntity.ok(usuarioService.toDTO(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> updateUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO datosActualizados, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        Usuario usuarioEnBBDD = usuarioService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("El usuario a modificar no existe."));
        
        if (usuarioEnBBDD.getEmail().equals(principal.getName())) {
            usuarioService.updateProfile(principal.getName(), datosActualizados.nombre(), datosActualizados.password(), datosActualizados.confirmPassword());
            
            Usuario actualizado = usuarioService.findByEmail(principal.getName()).get();
            return ResponseEntity.ok(usuarioService.toDTO(actualizado));
        } else {
            throw new NoSuchElementException("No tienes permisos para editar este usuario.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long id, Principal principal, HttpServletRequest request) {
        if (principal == null) return ResponseEntity.status(401).build();

        Usuario usuarioEnBBDD = usuarioService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("El usuario a borrar no existe."));
        
        boolean isOwner = usuarioEnBBDD.getEmail().equals(principal.getName());
        boolean isAdmin = request.isUserInRole("ROLE_ADMIN") || request.isUserInRole("ADMIN");

        if (isOwner || isAdmin) {
            usuarioService.deleteUserById(id);
            return ResponseEntity.noContent().build();
        } else {
            throw new NoSuchElementException("No tienes permisos para eliminar esta cuenta.");
        }
    }
}