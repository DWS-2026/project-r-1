package es.codeurjc.web.controller;

import java.security.Principal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.service.ConsejoService;
import es.codeurjc.web.service.TransaccionService;
import es.codeurjc.web.service.UsuarioService;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ConsejoService consejoService;

    @Autowired
    private TransaccionService transaccionService;

    @GetMapping("/register")
    public String mostrarRegistro(Model model) {
        return "register";
    }

    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        return "login";
    }

    // FIX: Leemos parámetros individuales para evitar vulnerabilidad de Mass Assignment (que inyecten id o roles)
    @PostMapping("/registro")
    public String registrarUsuario(Model model, @RequestParam String nombre, @RequestParam String email, @RequestParam String contrasena) {
        Usuario usuario = new Usuario(nombre, email, contrasena);
        if (!usuarioService.registrarNuevoUsuario(usuario)) {
            model.addAttribute("errorMsg", "Ese correo electrónico ya está en uso. Por favor, utiliza otro o inicia sesión.");
            return "register"; 
        }
        return "redirect:/"; 
    }

    @GetMapping("/profile-view")
    public String profileView(Model model, HttpServletRequest request) { 
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            Usuario user = usuarioService.findByEmail(principal.getName()).orElseThrow();
            model.addAttribute("userProfile", user);
            model.addAttribute("sellingAdvices", consejoService.findBySeller(user));
            model.addAttribute("purchasedTransactions", transaccionService.findByBuyer(user));
        }
        return "profile-view";
    }

    @GetMapping("/profile-edit")
    public String showProfileEditForm(Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            Usuario user = usuarioService.findByEmail(principal.getName()).orElseThrow();
            model.addAttribute("userProfile", user);
            return "profile-edit";
        }
        return "redirect:/login";
    }

    @PostMapping("/profile-edit")
    public String processProfileEdit(@RequestParam String nombre,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String confirmPassword,
            HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            usuarioService.updateProfile(principal.getName(), nombre, password, confirmPassword);
        }
        return "redirect:/profile-view";
    }

    @PostMapping("/profile-delete")
    public String deleteOwnAccount(HttpServletRequest request) throws Exception {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            usuarioService.deleteUserByEmail(principal.getName());
            request.logout();
        }
        return "redirect:/";
    }
}