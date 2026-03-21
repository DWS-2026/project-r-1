package es.codeurjc.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // first we inject the PasswordEncoder bean defined in SecurityConfig to be able to encrypt passwords before saving them to the database
    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean registrarNuevoUsuario(Usuario usuario) {

        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) { //first we check if the email is already registered
            return false;
        }

        // 2. Sobrescribimos la contraseña en texto plano por la versión encriptada
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));

        usuarioRepository.save(usuario);
        return true;
    }
}