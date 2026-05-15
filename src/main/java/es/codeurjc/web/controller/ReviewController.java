package es.codeurjc.web.controller;

import java.security.Principal;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import es.codeurjc.web.model.Advice;
import es.codeurjc.web.model.User;
import es.codeurjc.web.model.Review;
import es.codeurjc.web.service.AdviceService;
import es.codeurjc.web.service.UserService;
import es.codeurjc.web.service.ReviewService;

@Controller
public class ReviewController {

    @Autowired private ReviewService reviewService;
    @Autowired private AdviceService adviceService;
    @Autowired private UserService userService;

    @GetMapping("/review-create/{adviceId}")
    public String showCreateForm(@PathVariable Long adviceId, Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal == null) return "redirect:/login";

        User user = userService.findByEmail(principal.getName()).orElseThrow();
        Advice advice = adviceService.findById(adviceId).orElseThrow();

        Optional<Review> existingReview = reviewService.findByAuthorAndAdvice(user, advice);
        if (existingReview.isPresent()) {
            return "redirect:/review-edit/" + existingReview.get().getId();
        }

        model.addAttribute("advice", advice);
        return "review-create";
    }

    @PostMapping("/review-create/{adviceId}")
    public String processCreate(@PathVariable Long adviceId, @RequestParam String title, @RequestParam int score, @RequestParam String comment, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            reviewService.createReview(adviceId, principal.getName(), title, score, comment);
        }
        return "redirect:/profile-view";
    }

    @GetMapping("/review-edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            Optional<Review> v = reviewService.findById(id);
            if (v.isPresent() && v.get().getAuthor().getEmail().equals(principal.getName())) {
                model.addAttribute("review", v.get());
                return "review-edit";
            }
        }
        return "redirect:/profile-view";
    }

    @PostMapping("/review-edit/{id}")
    public String processEdit(@PathVariable Long id, @RequestParam String title, @RequestParam int score, @RequestParam String comment, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            reviewService.updateReview(id, principal.getName(), title, score, comment);
        }
        return "redirect:/profile-view";
    }

    @PostMapping("/review-delete/{id}")
    public String deleteReview(@PathVariable Long id, HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            reviewService.deleteReview(id, principal.getName());
        }
        return "redirect:/profile-view";
    }
}