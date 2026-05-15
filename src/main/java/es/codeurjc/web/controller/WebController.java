package es.codeurjc.web.controller;

import es.codeurjc.web.model.Advice;
import es.codeurjc.web.service.AdviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class WebController {

    @Autowired
    private AdviceService adviceService;

    @GetMapping("/")
    public String index(Model model) {
        List<Advice> advices = adviceService.findAll();
        model.addAttribute("advices", advices);
        return "index";
    }
}