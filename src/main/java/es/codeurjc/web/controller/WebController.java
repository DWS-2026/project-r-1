package es.codeurjc.web.controller;

import es.codeurjc.web.model.Consejo;
import es.codeurjc.web.service.ConsejoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class WebController {

    @Autowired
    private ConsejoService consejoService;

    @GetMapping("/")
    public String index(Model model) {
        List<Consejo> consejos = consejoService.findAll();
        model.addAttribute("consejos", consejos);
        return "index";
    }
}