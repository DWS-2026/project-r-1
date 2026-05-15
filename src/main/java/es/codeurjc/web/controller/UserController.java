package es.codeurjc.web.controller;

import java.security.Principal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import es.codeurjc.web.model.User;
import es.codeurjc.web.service.AdviceService;
import es.codeurjc.web.service.TransactionService;
import es.codeurjc.web.service.UserService;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AdviceService adviceService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/register")
    public String showRegister(Model model) {
        return "register";
    }

    @GetMapping("/login")
    public String showLogin(Model model) {
        return "login";
    }

    // FIX: We read individual parameters to avoid Mass Assignment vulnerability (injecting ids or roles)
    @PostMapping("/register")
    public String registerUser(Model model, @RequestParam String name, @RequestParam String email, @RequestParam String password) {
        User user = new User(name, email, password);
        if (!userService.registerNewUser(user)) {
            model.addAttribute("errorMsg", "That email is already in use. Please use another one or log in.");
            return "register"; 
        }
        return "redirect:/"; 
    }

    @GetMapping("/profile-view")
    public String profileView(Model model, HttpServletRequest request) { 
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            User user = userService.findByEmail(principal.getName()).orElseThrow();
            model.addAttribute("userProfile", user);
            model.addAttribute("sellingAdvices", adviceService.findBySeller(user));
            model.addAttribute("purchasedTransactions", transactionService.findByBuyer(user));
        }
        return "profile-view";
    }

    @GetMapping("/profile-edit")
    public String showProfileEditForm(Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            User user = userService.findByEmail(principal.getName()).orElseThrow();
            model.addAttribute("userProfile", user);
            return "profile-edit";
        }
        return "redirect:/login";
    }

    @PostMapping("/profile-edit")
    public String processProfileEdit(@RequestParam String name,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String confirmPassword,
            HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            userService.updateProfile(principal.getName(), name, password, confirmPassword);
        }
        return "redirect:/profile-view";
    }

    @PostMapping("/profile-delete")
    public String deleteOwnAccount(HttpServletRequest request) throws Exception {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            userService.deleteUserByEmail(principal.getName());
            request.logout();
        }
        return "redirect:/";
    }
}