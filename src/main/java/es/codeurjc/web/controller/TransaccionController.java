package es.codeurjc.web.controller;

import java.security.Principal;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import es.codeurjc.web.model.Consejo;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.service.ConsejoService;
import es.codeurjc.web.service.TransaccionService;
import es.codeurjc.web.service.UsuarioService;

@Controller
public class TransaccionController {

    @Autowired
    private TransaccionService transaccionService;

    @Autowired
    private ConsejoService consejoService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/transaction-create/{id}")
    public String showPaymentGateway(@PathVariable Long id, Model model) {
        Optional<Consejo> optionalConsejo = consejoService.findById(id);
        if (optionalConsejo.isPresent()) {
            model.addAttribute("consejo", optionalConsejo.get());
            return "transaction-create";
        }
        return "error";
    }

    @PostMapping("/transaction-create/{id}")
    public String processPayment(@PathVariable Long id, Principal principal) {
        if (principal == null) return "redirect:/login";

        boolean success = transaccionService.processPayment(id, principal.getName());
        if (!success) {
            return "redirect:/profile-view"; 
        }
        return "redirect:/transaction-view";
    }

    @GetMapping("/transaction-view")
    public String showTransactions(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        Usuario buyer = usuarioService.findByEmail(principal.getName()).orElseThrow();
        model.addAttribute("transactions", transaccionService.findByBuyer(buyer));
        return "transaction-view";
    }
}