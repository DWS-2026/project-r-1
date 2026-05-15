package es.codeurjc.web.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import es.codeurjc.web.model.Consejo;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.service.ConsejoService;
import es.codeurjc.web.service.UsuarioService;

@Controller
public class ConsejoController {

    @Autowired
    private ConsejoService consejoService;
    
    // Añadimos dependencia del servicio de usuario para las validaciones de acceso
    @Autowired
    private UsuarioService usuarioService;

    // FIX Mass Assignment: Ya no aceptamos la entidad completa por data binding.
    // Recibimos cada campo por separado y construimos manualmente el objeto.
    @PostMapping("/advice-create")
    public String createAdvice(@RequestParam String title,
                               @RequestParam String category,
                               @RequestParam double price,
                               @RequestParam String secretText,
                               HttpServletRequest request, 
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               @RequestParam(value = "attachmentFile", required = false) MultipartFile attachmentFile) throws IOException {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            Consejo consejo = new Consejo(title, category, price, secretText, null);
            consejoService.createAdvice(consejo, principal.getName(), imageFile, attachmentFile);
        }
        return "redirect:/profile-view"; 
    }

    @GetMapping("/advice/{id}/image")
    public ResponseEntity<byte[]> downloadImage(@PathVariable Long id) {
        Optional<Consejo> consejo = consejoService.findById(id);
        if (consejo.isPresent() && consejo.get().getImageBytes() != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(consejo.get().getImageBytes());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/advice/{id}/attachment")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<Consejo> consejoOpt = consejoService.findById(id);
        if (consejoOpt.isPresent()) {
            Consejo consejo = consejoOpt.get();
            Usuario user = usuarioService.findByEmail(principal.getName()).orElseThrow();
            
            // FIX: Validamos que el usuario tiene acceso al material (IDOR)
            boolean isSeller = consejo.getSeller().getId().equals(user.getId());
            boolean isAdmin = request.isUserInRole("ADMIN") || request.isUserInRole("ROLE_ADMIN");
            boolean hasBought = user.getCompras().stream().anyMatch(t -> t.getConsejo().getId().equals(id));
            
            if (isSeller || isAdmin || hasBought) {
                Resource file = consejoService.getAttachmentResource(id);
                if (file != null) {
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + consejo.getAttachmentName() + "\"")
                            .body(file);
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/advice-create")
    public String adviceCreate(Model model) {
        return "advice-create";
    }

    @GetMapping("/advice-detail/{id}")
    public String showAdviceDetail(@PathVariable Long id, Model model) {
        Optional<Consejo> optionalConsejo = consejoService.findById(id);
        if (optionalConsejo.isPresent()) {
            model.addAttribute("consejo", optionalConsejo.get());
            return "advice-detail";
        }
        return "error"; 
    }

    @PostMapping("/advice-delete/{id}")
    public String deleteAdvice(@PathVariable Long id, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            consejoService.deleteAdvice(id, principal.getName());
        }
        return "redirect:/profile-view";
    }

    @GetMapping("/advice-edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            Optional<Consejo> consejo = consejoService.findById(id);
            if (consejo.isPresent() && consejo.get().getSeller().getEmail().equals(principal.getName())) {
                model.addAttribute("consejo", consejo.get());
                return "advice-edit";
            }
        }
        return "redirect:/profile-view";
    }

    // FIX Mass Assignment: Mismo tratamiento que en advice-create. Parámetros individuales.
    @PostMapping("/advice-edit/{id}")
    public String processEditForm(@PathVariable Long id, 
                                  @RequestParam String title,
                                  @RequestParam String category,
                                  @RequestParam double price,
                                  @RequestParam String secretText,
                                  @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                  @RequestParam(value = "attachmentFile", required = false) MultipartFile attachmentFile, 
                                  HttpServletRequest request) throws IOException {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            Consejo consejoDetalles = new Consejo();
            consejoDetalles.setTitle(title);
            consejoDetalles.setCategory(category);
            consejoDetalles.setPrice(price);
            consejoDetalles.setSecretText(secretText);
            consejoService.updateAdvice(id, consejoDetalles, principal.getName(), imageFile, attachmentFile);
        }
        return "redirect:/profile-view";
    }

    @GetMapping("/advice-secret/{id}")
    public String viewSecretContent(@PathVariable Long id, Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return "redirect:/login";
        }
        Optional<Consejo> consejoOpt = consejoService.findById(id);
        if (consejoOpt.isPresent()) {
            Consejo consejo = consejoOpt.get();
            Usuario user = usuarioService.findByEmail(principal.getName()).orElseThrow();
            
            // FIX: Validar si ha pagado antes de mostrar la plantilla de contenido secreto
            boolean isSeller = consejo.getSeller().getId().equals(user.getId());
            boolean isAdmin = request.isUserInRole("ADMIN") || request.isUserInRole("ROLE_ADMIN");
            boolean hasBought = user.getCompras().stream().anyMatch(t -> t.getConsejo().getId().equals(id));
            
            if (isSeller || isAdmin || hasBought) {
                model.addAttribute("consejo", consejo);
                return "advice-secret";
            } else {
                return "redirect:/advice-detail/" + id;
            }
        }
        return "redirect:/profile-view";
    }
}