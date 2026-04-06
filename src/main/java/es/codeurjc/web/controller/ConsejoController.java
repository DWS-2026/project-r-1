package es.codeurjc.web.controller;

import es.codeurjc.web.model.Consejo;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.service.ConsejoService;
import es.codeurjc.web.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

@Controller
public class ConsejoController {

    @Autowired
    private ConsejoService consejoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/advice-create")
    public String createAdvice(Model model, Consejo consejo, HttpServletRequest request, 
                               @RequestParam("imageFile") MultipartFile imageFile) throws IOException { // <-- Añadido MultipartFile

        Principal principal = request.getUserPrincipal();
        
        if (principal != null) {
            Optional<Usuario> userOptional = usuarioRepository.findByEmail(principal.getName());
            
            if (userOptional.isPresent()) {
                Usuario seller = userOptional.get();
                consejo.setSeller(seller);
                
                // Extraemos los bytes de la imagen subida y los guardamos en el consejo
                if (!imageFile.isEmpty()) {
                    consejo.setImageBytes(imageFile.getBytes());
                }
                
                consejoService.saveConsejo(consejo);
            }
        }
        return "redirect:/profile-view"; 
    }

    // --- NUEVO MÉTODO: Sirve la imagen desde la BBDD al HTML ---
    @GetMapping("/advice/{id}/image")
    public ResponseEntity<byte[]> downloadImage(@PathVariable Long id) {
        Optional<Consejo> consejo = consejoService.findById(id);
        
        if (consejo.isPresent() && consejo.get().getImageBytes() != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg") // Le dice al navegador que es una imagen
                    .body(consejo.get().getImageBytes());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Mostrar formulario de creación
    @GetMapping("/advice-create")
    public String adviceCreate(Model model, org.springframework.security.web.csrf.CsrfToken csrfToken) {
        model.addAttribute("_csrf", csrfToken);
        return "advice-create";
    }

    // Mostrar detalles de un consejo
    @GetMapping("/advice-detail/{id}")
    public String showAdviceDetail(@PathVariable Long id, Model model) {
        Optional<Consejo> optionalConsejo = consejoService.findById(id);
        if (optionalConsejo.isPresent()) {
            model.addAttribute("consejo", optionalConsejo.get());
            return "advice-detail";
        } else {
            return "error"; 
        }
    }

    // --- 1. BORRAR CONSEJO ---
    @PostMapping("/advice-delete/{id}")
    public String deleteAdvice(@PathVariable Long id, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            Optional<Consejo> consejo = consejoService.findById(id);
            
            // Comprobamos que el consejo existe y que el usuario logueado es el dueño
            if (consejo.isPresent() && consejo.get().getSeller().getEmail().equals(principal.getName())) {
                consejoService.deleteById(id);
            }
        }
        return "redirect:/profile-view";
    }

    // --- 2. MOSTRAR PANTALLA DE EDITAR ---
    @GetMapping("/advice-edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, HttpServletRequest request, org.springframework.security.web.csrf.CsrfToken csrfToken) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            Optional<Consejo> consejo = consejoService.findById(id);
            
            // Solo dejamos entrar a la pantalla de edición si es el dueño
            if (consejo.isPresent() && consejo.get().getSeller().getEmail().equals(principal.getName())) {
                model.addAttribute("consejo", consejo.get());
                model.addAttribute("_csrf", csrfToken);
                return "advice-edit";
            }
        }
        return "redirect:/profile-view";
    }

    // --- 3. GUARDAR LOS CAMBIOS EDITADOS ---
    // --- 3. GUARDAR LOS CAMBIOS EDITADOS ---
    @PostMapping("/advice-edit/{id}")
    public String processEditForm(@PathVariable Long id, Consejo consejoDetalles, 
                                  @RequestParam("imageFile") MultipartFile imageFile, 
                                  HttpServletRequest request) throws IOException {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            Optional<Consejo> consejoOpcional = consejoService.findById(id);
            
            if (consejoOpcional.isPresent() && consejoOpcional.get().getSeller().getEmail().equals(principal.getName())) {
                Consejo consejoExistente = consejoOpcional.get();
                
                // Actualizamos los textos y precios
                consejoExistente.setTitle(consejoDetalles.getTitle());
                consejoExistente.setCategory(consejoDetalles.getCategory());
                consejoExistente.setPrice(consejoDetalles.getPrice());
                
                // AQUÍ ESTÁ EL CAMBIO: Usamos setSecretText y getSecretText
                consejoExistente.setSecretText(consejoDetalles.getSecretText()); 

                // Si el usuario subió una imagen nueva, la sobrescribimos
                if (!imageFile.isEmpty()) {
                    consejoExistente.setImageBytes(imageFile.getBytes());
                }

                // Guardamos en BBDD
                consejoService.saveConsejo(consejoExistente);
            }
        }
        return "redirect:/profile-view";
    }

    @GetMapping("/advice-secret/{id}")
    public String viewSecretContent(@PathVariable Long id, org.springframework.ui.Model model, java.security.Principal principal) {
        // Redirigir al login si el usuario no ha iniciado sesión
        if (principal == null) {
            return "redirect:/login";
        }

        java.util.Optional<es.codeurjc.web.model.Consejo> consejoOpt = consejoService.findById(id);
        
        if (consejoOpt.isPresent()) {
            model.addAttribute("consejo", consejoOpt.get());
            return "advice-secret";
        }
        
        // Si el consejo no existe, redirigimos al perfil
        return "redirect:/profile-view";
    }
}