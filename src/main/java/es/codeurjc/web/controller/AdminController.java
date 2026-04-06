package es.codeurjc.web.controller;

import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.repository.UsuarioRepository;
import es.codeurjc.web.service.ConsejoService;
import es.codeurjc.web.service.TransaccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin") // Todas las rutas de este archivo empezarán por /admin
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ConsejoService consejoService;
    @Autowired
    private TransaccionService transaccionService;

    // 1. Mostrar la lista de todos los usuarios
    @GetMapping("/users")
    public String listUsers(Model model, org.springframework.security.web.csrf.CsrfToken csrfToken) {
        model.addAttribute("usersList", usuarioRepository.findAll());
        // Pasamos el token de seguridad para que funcionen los botones de Banear
        model.addAttribute("_csrf", csrfToken);
        return "admin"; // Llamará a admin.html
    }

    // 2. Inspeccionar el perfil de un usuario concreto
    @GetMapping("/user/{id}")
    public String inspectUser(@PathVariable Long id, Model model) {
        Usuario userToInspect = usuarioRepository.findById(id).orElseThrow();

        // Le pasamos los datos del usuario inspeccionado a la plantilla del perfil
        model.addAttribute("userProfile", userToInspect);
        model.addAttribute("sellingAdvices", consejoService.findBySeller(userToInspect));
        model.addAttribute("purchasedTransactions", transaccionService.findByBuyer(userToInspect));

        // Añadimos una variable para que el HTML sepa que lo está viendo el admin
        // y oculte los botones de "Editar" o "Borrar" (ya que no es su perfil)
        model.addAttribute("isAdminInspecting", true);

        return "profile-view"; // ¡Reutilizamos tu diseño del perfil!
    }

    // 3. Borrar (Banear) a un usuario
    @PostMapping("/user-delete/{id}")
    public String banUser(@PathVariable Long id) {
        // Al usar deleteById, Hibernate va a la BBDD y, gracias al CascadeType.ALL,
        // borra al usuario y todos los consejos/valoraciones asociados a él.
        usuarioRepository.deleteById(id);

        return "redirect:/admin/users"; // Recargamos la tabla
    }
}