package es.codeurjc.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public void registrarNuevoUsuario(Usuario usuario) {
        // Later, you can add logic here (like password hashing)
        // For now, it just saves the user to the database
        usuarioRepository.save(usuario);
    }
}