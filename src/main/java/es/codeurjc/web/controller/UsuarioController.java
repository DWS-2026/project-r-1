package es.codeurjc.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.service.UsuarioService;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/registro")
    public String registrarUsuario(Model model, Usuario usuario) {
        
        // 1. The controller receives the 'usuario' object from the HTML form.
        // 2. It passes the object to the Service layer.
        if (!usuarioService.registrarNuevoUsuario(usuario)) {
            return "error";
        }

        // 3. It redirects the browser back to the main page.
        return "redirect:/"; 
    }
}