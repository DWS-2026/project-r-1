package es.codeurjc.web.restcontroller;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.validation.Valid;

import es.codeurjc.web.dto.ReviewDTO;
import es.codeurjc.web.model.User;
import es.codeurjc.web.model.Review;
import es.codeurjc.web.service.UserService;
import es.codeurjc.web.service.ReviewService;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewRestController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public Page<ReviewDTO> getAllReviews(Pageable pageable) {
        return reviewService.findAll(pageable);
    }

    @GetMapping("/me")
    public ResponseEntity<List<ReviewDTO>> getMyReviews(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        User author = userService.findByEmail(principal.getName()).orElseThrow();
        List<ReviewDTO> myReviews = reviewService.findByAuthor(author).stream()
                .map(reviewService::toDTO).toList();
        return ResponseEntity.ok(myReviews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> getReview(@PathVariable Long id) {
        Review review = reviewService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("The review with ID " + id + " does not exist."));
        return ResponseEntity.ok(reviewService.toDTO(review));
    }

    @PostMapping("/")
    public ResponseEntity<Void> createReview(@RequestParam Long adviceId, @Valid @RequestBody ReviewDTO reviewDTO, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        try {
            Review v = reviewService.createReview(adviceId, principal.getName(), reviewDTO.title(), reviewDTO.score(), reviewDTO.comment());
            
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(v.getId())
                    .toUri();
                    
            return ResponseEntity.created(location).build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not process the review. Verify that the advice exists.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateReview(@PathVariable Long id, @Valid @RequestBody ReviewDTO reviewDTO, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        boolean updated = reviewService.updateReview(id, principal.getName(), reviewDTO.title(), reviewDTO.score(), reviewDTO.comment());
        if (updated) {
            return ResponseEntity.ok().build();
        } else {
            throw new NoSuchElementException("Review not found or you do not have permission to edit it.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        boolean deleted = reviewService.deleteReview(id, principal.getName());
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            throw new NoSuchElementException("Review not found or you do not have permission to delete it.");
        }
    }
}