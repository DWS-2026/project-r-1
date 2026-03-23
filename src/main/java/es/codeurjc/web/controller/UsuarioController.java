package es.codeurjc.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.service.UsuarioService;
import es.codeurjc.web.service.ConsejoService;
import es.codeurjc.web.service.TransaccionService;
import es.codeurjc.web.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;

@Controller
public class UsuarioController {
    
    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ConsejoService consejoService;

    @Autowired
    private TransaccionService transaccionService;

    @GetMapping("/register")
    public String mostrarRegistro(Model model, CsrfToken csrfToken) {
        model.addAttribute("_csrf", csrfToken);
        return "register"; 
    }

    @GetMapping("/login")
    public String mostrarLogin(Model model, CsrfToken csrfToken) {
        model.addAttribute("_csrf", csrfToken);
        return "login"; 
    }

    @PostMapping("/registro")
    public String registrarUsuario(Model model, Usuario usuario) {
        if (!usuarioService.registrarNuevoUsuario(usuario)) {
            return "error";
        }
        return "redirect:/"; 
    }

    @GetMapping("/profile-view")
    public String profileView(Model model, HttpServletRequest request, CsrfToken csrfToken) { // <-- AÑADIDO CsrfToken
        Principal principal = request.getUserPrincipal();
        
        if (principal != null) {
            Usuario user = usuarioRepository.findByEmail(principal.getName()).orElseThrow();
            
            model.addAttribute("userProfile", user);
            model.addAttribute("sellingAdvices", consejoService.findBySeller(user));
            model.addAttribute("purchasedTransactions", transaccionService.findByBuyer(user));
            
            // Pasamos el token de seguridad a la plantilla para el botón de Borrar
            model.addAttribute("_csrf", csrfToken); // <-- AÑADIDO
        }
        
        return "profile-view";
    }

    // --- 1. MOSTRAR PANTALLA DE EDITAR PERFIL ---
    @GetMapping("/profile-edit")
    public String showProfileEditForm(Model model, HttpServletRequest request, CsrfToken csrfToken) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            Usuario user = usuarioRepository.findByEmail(principal.getName()).orElseThrow();
            model.addAttribute("userProfile", user);
            model.addAttribute("_csrf", csrfToken);
            return "profile-edit";
        }
        return "redirect:/login";
    }

    // --- 2. GUARDAR LOS CAMBIOS DEL PERFIL ---
    @PostMapping("/profile-edit")
    public String processProfileEdit(@RequestParam String nombre, 
                                     @RequestParam(required = false) String password,
                                     @RequestParam(required = false) String confirmPassword,
                                     HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        
        if (principal != null) {
            Usuario user = usuarioRepository.findByEmail(principal.getName()).orElseThrow();
            
            // Actualizamos el nombre de usuario
            user.setNombre(nombre);
            
            // Comprobamos si ha escrito una contraseña nueva y si ambas coinciden
            if (password != null && !password.isEmpty() && password.equals(confirmPassword)) {
                user.setContrasena(passwordEncoder.encode(password));
            }
            
            // Guardamos el usuario actualizado en la base de datos
            usuarioRepository.save(user);
        }
        
        return "redirect:/profile-view";
    }

}