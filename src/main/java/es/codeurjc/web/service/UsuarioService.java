package es.codeurjc.web.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.repository.UsuarioRepository;
import es.codeurjc.web.dto.UsuarioDTO;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- Métodos de conversión a DTO ---
    public UsuarioDTO toDTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRoles()
        );
    }

    public boolean registrarNuevoUsuario(Usuario usuario) {
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            return false;
        }
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        usuarioRepository.save(usuario);
        return true;
    }

    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    // Nuevo método para soportar paginación en la API REST
    public Page<UsuarioDTO> findAll(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(this::toDTO);
    }

    public void updateProfile(String email, String nombre, String password, String confirmPassword) {
        Usuario user = usuarioRepository.findByEmail(email).orElseThrow();
        user.setNombre(nombre);

        if (password != null && !password.isEmpty() && password.equals(confirmPassword)) {
            user.setContrasena(passwordEncoder.encode(password));
        }
        usuarioRepository.save(user);
    }

    public void deleteUserByEmail(String email) {
        Usuario user = usuarioRepository.findByEmail(email).orElseThrow();
        usuarioRepository.delete(user);
    }

    public void deleteUserById(Long id) {
        usuarioRepository.deleteById(id);
    }
}