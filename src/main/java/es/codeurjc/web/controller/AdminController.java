package es.codeurjc.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.service.ConsejoService;
import es.codeurjc.web.service.TransaccionService;
import es.codeurjc.web.service.UsuarioService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private ConsejoService consejoService;
    
    @Autowired
    private TransaccionService transaccionService;

    @GetMapping("/users")
    public String listUsers(Model model, org.springframework.security.web.csrf.CsrfToken csrfToken) {
        model.addAttribute("usersList", usuarioService.findAll());
        model.addAttribute("_csrf", csrfToken);
        return "admin";
    }

    @GetMapping("/user/{id}")
    public String inspectUser(@PathVariable Long id, Model model, org.springframework.security.web.csrf.CsrfToken csrfToken) {
        Usuario userToInspect = usuarioService.findById(id).orElseThrow();
        model.addAttribute("userProfile", userToInspect);
        model.addAttribute("sellingAdvices", consejoService.findBySeller(userToInspect));
        model.addAttribute("purchasedTransactions", transaccionService.findByBuyer(userToInspect));
        model.addAttribute("isAdminInspecting", true);
        model.addAttribute("_csrf", csrfToken);
        return "profile-view";
    }

    @PostMapping("/user-delete/{id}")
    public String banUser(@PathVariable Long id) {
        usuarioService.deleteUserById(id);
        return "redirect:/admin/users";
    }
}