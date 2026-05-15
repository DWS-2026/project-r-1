package es.codeurjc.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import es.codeurjc.web.model.User;
import es.codeurjc.web.service.AdviceService;
import es.codeurjc.web.service.TransactionService;
import es.codeurjc.web.service.UserService;

@Controller
@RequestMapping("/admin")
// FIX Defense in Depth: Even if the URL is protected in SecurityConfig,
// we reinforce that all methods in this controller require the ADMIN role.
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private AdviceService adviceService;
    
    @Autowired
    private TransactionService transactionService;

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("usersList", userService.findAll());
        return "admin";
    }

    @GetMapping("/user/{id}")
    public String inspectUser(@PathVariable Long id, Model model) {
        User userToInspect = userService.findById(id).orElseThrow();
        model.addAttribute("userProfile", userToInspect);
        model.addAttribute("sellingAdvices", adviceService.findBySeller(userToInspect));
        model.addAttribute("purchasedTransactions", transactionService.findByBuyer(userToInspect));
        model.addAttribute("isAdminInspecting", true);
        return "profile-view";
    }

    @PostMapping("/user-delete/{id}")
    public String banUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return "redirect:/admin/users";
    }
}