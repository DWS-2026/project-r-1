package es.codeurjc.web.controller;

import java.security.Principal;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import es.codeurjc.web.model.Advice;
import es.codeurjc.web.model.User;
import es.codeurjc.web.model.Transaction;
import es.codeurjc.web.service.AdviceService;
import es.codeurjc.web.service.TransactionService;
import es.codeurjc.web.service.UserService;

@Controller
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AdviceService adviceService;

    @Autowired
    private UserService userService;

    @GetMapping("/transaction-create/{id}")
    public String showPaymentGateway(@PathVariable Long id, Model model) {
        Optional<Advice> optionalAdvice = adviceService.findById(id);
        if (optionalAdvice.isPresent()) {
            model.addAttribute("advice", optionalAdvice.get());
            return "transaction-create";
        }
        return "error";
    }

    @PostMapping("/transaction-create/{id}")
    public String processPayment(@PathVariable Long id, Principal principal) {
        if (principal == null) return "redirect:/login";

        // Adjusted for the new service return value
        Transaction success = transactionService.processPayment(id, principal.getName());
        if (success == null) {
            return "redirect:/profile-view"; 
        }
        return "redirect:/transaction-view";
    }

    @GetMapping("/transaction-view")
    public String showTransactions(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User buyer = userService.findByEmail(principal.getName()).orElseThrow();
        model.addAttribute("transactions", transactionService.findByBuyer(buyer));
        return "transaction-view";
    }
}