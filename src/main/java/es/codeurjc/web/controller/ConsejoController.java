package es.codeurjc.web.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;
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
import es.codeurjc.web.model.Consejo;
import es.codeurjc.web.service.ConsejoService;

@Controller
public class ConsejoController {

    @Autowired
    private ConsejoService consejoService;

    @PostMapping("/advice-create")
    public String createAdvice(Consejo consejo, HttpServletRequest request, 
                               @RequestParam("imageFile") MultipartFile imageFile) throws IOException {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            consejoService.createAdvice(consejo, principal.getName(), imageFile);
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

    @PostMapping("/advice-edit/{id}")
    public String processEditForm(@PathVariable Long id, Consejo consejoDetalles, 
                                  @RequestParam("imageFile") MultipartFile imageFile, 
                                  HttpServletRequest request) throws IOException {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            consejoService.updateAdvice(id, consejoDetalles, principal.getName(), imageFile);
        }
        return "redirect:/profile-view";
    }

    @GetMapping("/advice-secret/{id}")
    public String viewSecretContent(@PathVariable Long id, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        Optional<Consejo> consejoOpt = consejoService.findById(id);
        if (consejoOpt.isPresent()) {
            model.addAttribute("consejo", consejoOpt.get());
            return "advice-secret";
        }
        return "redirect:/profile-view";
    }
}