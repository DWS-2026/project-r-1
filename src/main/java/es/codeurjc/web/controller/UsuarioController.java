package es.codeurjc.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.service.UsuarioService;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // this method is called when the user wants to view the registration page
    @GetMapping("/register")
    public String mostrarRegistro(Model model, CsrfToken csrfToken) {
        model.addAttribute("_csrf", csrfToken);
        return "register"; 
    }

    // this method is called when the user wants to view the login page
    @GetMapping("/login")
    public String mostrarLogin(Model model, CsrfToken csrfToken) {
        model.addAttribute("_csrf", csrfToken);
        return "login"; 
    }

    // this method is called when the user submits the registration form
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