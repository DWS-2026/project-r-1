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
import es.codeurjc.web.model.Advice;
import es.codeurjc.web.model.User;
import es.codeurjc.web.service.AdviceService;
import es.codeurjc.web.service.UserService;

@Controller
public class AdviceController {

    @Autowired
    private AdviceService adviceService;
    
    // Adding user service dependency for access validations
    @Autowired
    private UserService userService;

    // FIX Mass Assignment: We no longer accept the complete entity via data binding.
    // We receive each field separately and manually build the object.
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
            Advice advice = new Advice(title, category, price, secretText, null);
            adviceService.createAdvice(advice, principal.getName(), imageFile, attachmentFile);
        }
        return "redirect:/profile-view"; 
    }

    @GetMapping("/advice/{id}/image")
    public ResponseEntity<byte[]> downloadImage(@PathVariable Long id) {
        Optional<Advice> advice = adviceService.findById(id);
        if (advice.isPresent() && advice.get().getImageBytes() != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(advice.get().getImageBytes());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/advice/{id}/attachment")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<Advice> adviceOpt = adviceService.findById(id);
        if (adviceOpt.isPresent()) {
            Advice advice = adviceOpt.get();
            User user = userService.findByEmail(principal.getName()).orElseThrow();
            
            // FIX: Validate that the user has access to the material (IDOR)
            boolean isSeller = advice.getSeller().getId().equals(user.getId());
            boolean isAdmin = request.isUserInRole("ADMIN") || request.isUserInRole("ROLE_ADMIN");
            boolean hasBought = user.getPurchases().stream().anyMatch(t -> t.getAdvice().getId().equals(id));
            
            if (isSeller || isAdmin || hasBought) {
                Resource file = adviceService.getAttachmentResource(id);
                if (file != null) {
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + advice.getAttachmentName() + "\"")
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
        Optional<Advice> optionalAdvice = adviceService.findById(id);
        if (optionalAdvice.isPresent()) {
            model.addAttribute("advice", optionalAdvice.get());
            return "advice-detail";
        }
        return "error"; 
    }

    @PostMapping("/advice-delete/{id}")
    public String deleteAdvice(@PathVariable Long id, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            adviceService.deleteAdvice(id, principal.getName());
        }
        return "redirect:/profile-view";
    }

    @GetMapping("/advice-edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            Optional<Advice> advice = adviceService.findById(id);
            if (advice.isPresent() && advice.get().getSeller().getEmail().equals(principal.getName())) {
                model.addAttribute("advice", advice.get());
                return "advice-edit";
            }
        }
        return "redirect:/profile-view";
    }

    // FIX Mass Assignment: Same handling as in advice-create. Individual parameters.
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
            Advice adviceDetails = new Advice();
            adviceDetails.setTitle(title);
            adviceDetails.setCategory(category);
            adviceDetails.setPrice(price);
            adviceDetails.setSecretText(secretText);
            adviceService.updateAdvice(id, adviceDetails, principal.getName(), imageFile, attachmentFile);
        }
        return "redirect:/profile-view";
    }

    @GetMapping("/advice-secret/{id}")
    public String viewSecretContent(@PathVariable Long id, Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return "redirect:/login";
        }
        Optional<Advice> adviceOpt = adviceService.findById(id);
        if (adviceOpt.isPresent()) {
            Advice advice = adviceOpt.get();
            User user = userService.findByEmail(principal.getName()).orElseThrow();
            
            // FIX: Validate if paid before showing the secret content template
            boolean isSeller = advice.getSeller().getId().equals(user.getId());
            boolean isAdmin = request.isUserInRole("ADMIN") || request.isUserInRole("ROLE_ADMIN");
            boolean hasBought = user.getPurchases().stream().anyMatch(t -> t.getAdvice().getId().equals(id));
            
            if (isSeller || isAdmin || hasBought) {
                model.addAttribute("advice", advice);
                return "advice-secret";
            } else {
                return "redirect:/advice-detail/" + id;
            }
        }
        return "redirect:/profile-view";
    }
}