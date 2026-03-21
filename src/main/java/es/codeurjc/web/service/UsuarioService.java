package es.codeurjc.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public boolean registrarNuevoUsuario(Usuario usuario) {

        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) { //first we check if the email is already registered
            return false;
        }

        usuarioRepository.save(usuario);
        return true;
    }
}