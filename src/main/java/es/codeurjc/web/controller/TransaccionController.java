package es.codeurjc.web.controller;

import es.codeurjc.web.model.Consejo;
import es.codeurjc.web.model.Transaccion;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.service.ConsejoService;
import es.codeurjc.web.service.TransaccionService;
import es.codeurjc.web.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import java.security.Principal;
import java.util.Optional;

@Controller
public class TransaccionController {

    @Autowired
    private TransaccionService transaccionService;

    @Autowired
    private ConsejoService consejoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/transaction-create/{id}")
    public String showPaymentGateway(@PathVariable Long id, Model model, CsrfToken csrfToken) {
        Optional<Consejo> optionalConsejo = consejoService.findById(id);
        if (optionalConsejo.isPresent()) {
            model.addAttribute("consejo", optionalConsejo.get());
            model.addAttribute("_csrf", csrfToken);
            return "transaction-create";
        }
        return "error";
    }

    @PostMapping("/transaction-create/{id}")
    public String processPayment(@PathVariable Long id, Principal principal) {
        if (principal == null) return "redirect:/login";

        Optional<Consejo> optionalConsejo = consejoService.findById(id);
        Optional<Usuario> optionalUser = usuarioRepository.findByEmail(principal.getName());

        if (optionalConsejo.isPresent() && optionalUser.isPresent()) {
            Consejo consejo = optionalConsejo.get();
            Usuario buyer = optionalUser.get();

            if (consejo.getSeller() != null && consejo.getSeller().getId().equals(buyer.getId())) {
                return "redirect:/profile-view"; 
            }

            Transaccion transaccion = new Transaccion(buyer, consejo, consejo.getPrice());
            transaccionService.save(transaccion);
            return "redirect:/transaction-view";
        }
        return "error";
    }

    @GetMapping("/transaction-view")
    public String showTransactions() {
        return "transaction-view";
    }
}