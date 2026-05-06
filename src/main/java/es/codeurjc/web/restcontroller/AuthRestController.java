package es.codeurjc.web.restcontroller;

import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.service.UsuarioService;
import es.codeurjc.web.security.jwt.AuthResponse;
import es.codeurjc.web.security.jwt.JwtTokenProvider;
import es.codeurjc.web.security.jwt.LoginRequest;
import es.codeurjc.web.security.jwt.SignupRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthRestController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        UserDetails user = (UserDetails) auth.getPrincipal();
        List<String> roles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        
        String token = jwtTokenProvider.generateToken(user.getUsername(), roles);

        return ResponseEntity.ok(new AuthResponse(token));
    }

    // --- NUEVO ENDPOINT DE REGISTRO ---
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        Usuario nuevoUsuario = new Usuario(request.getNombre(), request.getEmail(), request.getPassword());
        boolean registrado = usuarioService.registrarNuevoUsuario(nuevoUsuario);
        
        if (!registrado) {
            return ResponseEntity.badRequest().body("El email ya está en uso");
        }
        
        return ResponseEntity.status(201).body("Usuario registrado con éxito");
    }
}