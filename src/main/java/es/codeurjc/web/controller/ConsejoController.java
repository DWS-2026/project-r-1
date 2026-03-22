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
}