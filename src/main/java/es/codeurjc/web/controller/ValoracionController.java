package es.codeurjc.web.controller;

import es.codeurjc.web.model.Consejo;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.model.Valoracion;
import es.codeurjc.web.service.ConsejoService;
import es.codeurjc.web.service.ValoracionService;
import es.codeurjc.web.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Optional;

@Controller
public class ValoracionController {

    @Autowired private ValoracionService valoracionService;
    @Autowired private ConsejoService consejoService;
    @Autowired private UsuarioRepository usuarioRepository;

    // --- 1. MOSTRAR PANTALLA DE CREAR (O REDIRIGIR A EDITAR) ---
    @GetMapping("/review-create/{consejoId}")
    public String showCreateForm(@PathVariable Long consejoId, Model model, HttpServletRequest request, CsrfToken csrfToken) {
        Principal principal = request.getUserPrincipal();
        if (principal == null) return "redirect:/login";

        Usuario user = usuarioRepository.findByEmail(principal.getName()).orElseThrow();
        Consejo consejo = consejoService.findById(consejoId).orElseThrow();

        // ¡EL TRUCO! Si ya lo valoró, le mandamos directamente a la pantalla de edición
        Optional<Valoracion> existingReview = valoracionService.findByAuthorAndConsejo(user, consejo);
        if (existingReview.isPresent()) {
            return "redirect:/review-edit/" + existingReview.get().getId();
        }

        model.addAttribute("consejo", consejo);
        model.addAttribute("_csrf", csrfToken);
        return "review-create";
    }

    // --- 2. GUARDAR NUEVA VALORACIÓN ---
    @PostMapping("/review-create/{consejoId}")
    public String processCreate(@PathVariable Long consejoId, @RequestParam String title, @RequestParam int score, @RequestParam String comment, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            Usuario user = usuarioRepository.findByEmail(principal.getName()).orElseThrow();
            Consejo consejo = consejoService.findById(consejoId).orElseThrow();
            
            Valoracion v = new Valoracion(user, consejo, score, title, comment);
            valoracionService.save(v);
        }
        return "redirect:/profile-view";
    }

    // --- 3. MOSTRAR PANTALLA DE EDITAR ---
    @GetMapping("/review-edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, HttpServletRequest request, CsrfToken csrfToken) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            Optional<Valoracion> v = valoracionService.findById(id);
            // Comprobamos que es suya
            if (v.isPresent() && v.get().getAuthor().getEmail().equals(principal.getName())) {
                model.addAttribute("review", v.get());
                model.addAttribute("_csrf", csrfToken);
                return "review-edit";
            }
        }
        return "redirect:/profile-view";
    }

    // --- 4. GUARDAR CAMBIOS DE LA VALORACIÓN ---
    @PostMapping("/review-edit/{id}")
    public String processEdit(@PathVariable Long id, @RequestParam String title, @RequestParam int score, @RequestParam String comment, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            Optional<Valoracion> vOpcional = valoracionService.findById(id);
            if (vOpcional.isPresent() && vOpcional.get().getAuthor().getEmail().equals(principal.getName())) {
                Valoracion v = vOpcional.get();
                v.setTitle(title);
                v.setScore(score);
                v.setComment(comment);
                valoracionService.save(v);
            }
        }
        return "redirect:/profile-view";
    }

    // --- 5. BORRAR VALORACIÓN ---
    @PostMapping("/review-delete/{id}")
    public String deleteReview(@PathVariable Long id, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            Optional<Valoracion> vOpcional = valoracionService.findById(id);
            if (vOpcional.isPresent() && vOpcional.get().getAuthor().getEmail().equals(principal.getName())) {
                valoracionService.deleteById(id);
            }
        }
        return "redirect:/profile-view";
    }
}