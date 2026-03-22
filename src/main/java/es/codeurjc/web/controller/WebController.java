package es.codeurjc.web.controller;

import es.codeurjc.web.model.Consejo;
import es.codeurjc.web.service.ConsejoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.web.csrf.CsrfToken;
import java.util.List;

@Controller
public class WebController {

    // 1. Inyectamos el servicio para poder acceder a los consejos
    @Autowired
    private ConsejoService consejoService;

    @GetMapping("/")
    public String index(Model model) {
        // 2. Obtenemos todos los consejos de la base de datos
        // NOTA: Asegúrate de tener un método findAll() en tu ConsejoService
        List<Consejo> consejos = consejoService.findAll();
        
        // 3. Añadimos la lista de consejos al modelo para que Mustache pueda usarla
        model.addAttribute("consejos", consejos);
        
        return "index";
    }

    @GetMapping("/profile-view")
    public String profileView(Model model) {
        return "profile-view";
    }

    @GetMapping("/advice-create")
    public String adviceCreate(Model model, CsrfToken csrfToken) {
        model.addAttribute("_csrf", csrfToken);
        return "advice-create";
    }
}