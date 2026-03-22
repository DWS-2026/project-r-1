package es.codeurjc.web.controller;

import es.codeurjc.web.model.Consejo;
import es.codeurjc.web.model.Transaccion;
import es.codeurjc.web.service.ConsejoService;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/advice-detail/{id}")
    public String showAdviceDetail(@PathVariable Long id, Model model) {
        // 1. Buscamos el consejo por su ID
        Optional<Consejo> optionalConsejo = consejoService.findById(id);
        
        // 2. Si existe, lo añadimos al modelo y mostramos la página
        if (optionalConsejo.isPresent()) {
            Consejo consejo = optionalConsejo.get();
            model.addAttribute("consejo", consejo);
            return "advice-detail"; // Asegúrate de que tu archivo se llama advice-detail.html
        } else {
            // 3. Si alguien escribe un ID que no existe, le mostramos la página de error
            return "error"; 
        }
    }

    @Autowired
    private es.codeurjc.web.service.TransaccionService transaccionService;

    @Autowired
    private es.codeurjc.web.repository.UsuarioRepository usuarioRepository;

    // 1. Mostrar la pasarela de pago para un consejo específico
    @GetMapping("/transaction-create/{id}")
    public String showPaymentGateway(@PathVariable Long id, Model model, CsrfToken csrfToken) {
        Optional<Consejo> optionalConsejo = consejoService.findById(id);
        
        if (optionalConsejo.isPresent()) {
            model.addAttribute("consejo", optionalConsejo.get());
            model.addAttribute("_csrf", csrfToken);
            return "transaction-create";
        }
        return "error";
    }

    // 2. Procesar el pago y crear la transacción
    @PostMapping("/transaction-create/{id}")
    public String processPayment(@PathVariable Long id, java.security.Principal principal) {
        if (principal == null) return "redirect:/login";

        Optional<Consejo> optionalConsejo = consejoService.findById(id);
        Optional<es.codeurjc.web.model.Usuario> optionalUser = usuarioRepository.findByEmail(principal.getName());

        if (optionalConsejo.isPresent() && optionalUser.isPresent()) {
            Consejo consejo = optionalConsejo.get();
            es.codeurjc.web.model.Usuario buyer = optionalUser.get();

            // Evitar que el usuario compre su propio consejo (opcional pero buena práctica)
            if (consejo.getSeller() != null && consejo.getSeller().getId().equals(buyer.getId())) {
                return "redirect:/profile-view"; // O redirigir a una página de error
            }

            // Crear y guardar la transacción
            Transaccion transaccion = new Transaccion(buyer, consejo, consejo.getPrice());
            transaccionService.save(transaccion);

            // Redirigimos al historial de compras
            return "redirect:/transaction-view";
        }
        return "error";
    }

    // 3. Mostrar el historial de transacciones vacío (lo rellenaremos en el futuro)
    @GetMapping("/transaction-view")
    public String showTransactions() {
        return "transaction-view";
    }
}