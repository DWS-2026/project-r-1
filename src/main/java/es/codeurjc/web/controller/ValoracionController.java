package es.codeurjc.web.controller;

import java.security.Principal;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import es.codeurjc.web.model.Consejo;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.model.Valoracion;
import es.codeurjc.web.service.ConsejoService;
import es.codeurjc.web.service.UsuarioService;
import es.codeurjc.web.service.ValoracionService;

@Controller
public class ValoracionController {

    @Autowired private ValoracionService valoracionService;
    @Autowired private ConsejoService consejoService;
    @Autowired private UsuarioService usuarioService;

    @GetMapping("/review-create/{consejoId}")
    public String showCreateForm(@PathVariable Long consejoId, Model model, HttpServletRequest request, CsrfToken csrfToken) {
        Principal principal = request.getUserPrincipal();
        if (principal == null) return "redirect:/login";

        Usuario user = usuarioService.findByEmail(principal.getName()).orElseThrow();
        Consejo consejo = consejoService.findById(consejoId).orElseThrow();

        Optional<Valoracion> existingReview = valoracionService.findByAuthorAndConsejo(user, consejo);
        if (existingReview.isPresent()) {
            return "redirect:/review-edit/" + existingReview.get().getId();
        }

        model.addAttribute("consejo", consejo);
        model.addAttribute("_csrf", csrfToken);
        return "review-create";
    }

    @PostMapping("/review-create/{consejoId}")
    public String processCreate(@PathVariable Long consejoId, @RequestParam String title, @RequestParam int score, @RequestParam String comment, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            valoracionService.createReview(consejoId, principal.getName(), title, score, comment);
        }
        return "redirect:/profile-view";
    }

    @GetMapping("/review-edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, HttpServletRequest request, CsrfToken csrfToken) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            Optional<Valoracion> v = valoracionService.findById(id);
            if (v.isPresent() && v.get().getAuthor().getEmail().equals(principal.getName())) {
                model.addAttribute("review", v.get());
                model.addAttribute("_csrf", csrfToken);
                return "review-edit";
            }
        }
        return "redirect:/profile-view";
    }

    @PostMapping("/review-edit/{id}")
    public String processEdit(@PathVariable Long id, @RequestParam String title, @RequestParam int score, @RequestParam String comment, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            valoracionService.updateReview(id, principal.getName(), title, score, comment);
        }
        return "redirect:/profile-view";
    }

    @PostMapping("/review-delete/{id}")
    public String deleteReview(@PathVariable Long id, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            valoracionService.deleteReview(id, principal.getName());
        }
        return "redirect:/profile-view";
    }
}