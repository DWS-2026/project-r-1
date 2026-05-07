package es.codeurjc.web.controller;

import java.security.Principal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.security.web.csrf.CsrfToken;

// Restringimos el Advice solo a este paquete para evitar que rompa el controlador interno de OpenAPI
@ControllerAdvice(basePackages = "es.codeurjc.web.controller")
public class GlobalControllerAdvice {

    @ModelAttribute
    public void addAttributes(Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        
        if (principal != null) {
            // if the user is authenticated
            model.addAttribute("logged", true);
            model.addAttribute("userName", principal.getName());
            model.addAttribute("admin", request.isUserInRole("ADMIN"));
        } else {
            // if the user is not authenticated
            model.addAttribute("logged", false);
        }

        // Inyección global del token CSRF para todos los formularios de la web
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
    }
}