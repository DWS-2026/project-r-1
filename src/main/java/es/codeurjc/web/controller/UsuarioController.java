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

    // 1. Método que muestra la pantalla de registro y le pasa el token de seguridad
    @GetMapping("/register")
    public String mostrarRegistro(Model model, CsrfToken csrfToken) {
        model.addAttribute("_csrf", csrfToken);
        return "register"; 
    }

    // 2. Método que muestra la pantalla de login y le pasa el token de seguridad
    @GetMapping("/login")
    public String mostrarLogin(Model model, CsrfToken csrfToken) {
        model.addAttribute("_csrf", csrfToken);
        return "login"; 
    }

    // 3. Método que procesa el formulario cuando el usuario pulsa "Registrarse"
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