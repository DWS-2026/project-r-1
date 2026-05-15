package es.codeurjc.web.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import es.codeurjc.web.model.Advice;
import es.codeurjc.web.model.User;
import es.codeurjc.web.model.Review;
import es.codeurjc.web.repository.ReviewRepository;
import es.codeurjc.web.dto.ReviewDTO;
import es.codeurjc.web.dto.ReviewMapper;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AdviceService adviceService;

    @Autowired
    private ReviewMapper reviewMapper;

    public ReviewDTO toDTO(Review v) {
        return reviewMapper.toDTO(v);
    }

    public void save(Review review) {
        reviewRepository.save(review);
    }

    public Optional<Review> findById(Long id) {
        return reviewRepository.findById(id);
    }

    public Optional<Review> findByAuthorAndAdvice(User author, Advice advice) {
        return reviewRepository.findByAuthorAndAdvice(author, advice);
    }

    public List<Review> findByAuthor(User author) {
        return reviewRepository.findByAuthor(author);
    }

    public Page<ReviewDTO> findAll(Pageable pageable) {
        return reviewRepository.findAll(pageable).map(reviewMapper::toDTO);
    }

    public Review createReview(Long adviceId, String userEmail, String title, int score, String comment) {
        User user = userService.findByEmail(userEmail).orElseThrow();
        Advice advice = adviceService.findById(adviceId).orElseThrow();

        // FIX IDOR: Prevents someone from reviewing something they haven't bought.
        boolean hasBought = user.getPurchases().stream().anyMatch(t -> t.getAdvice().getId().equals(adviceId));
        boolean isSeller = advice.getSeller().getId().equals(user.getId());

        if (!hasBought && !isSeller) {
            throw new SecurityException("Access denied: You cannot review advice you haven't acquired.");
        }

        Review v = new Review(user, advice, score, title, comment);
        return reviewRepository.save(v);
    }

    public boolean updateReview(Long id, String userEmail, String title, int score, String comment) {
        Optional<Review> vOptional = reviewRepository.findById(id);
        if (vOptional.isPresent() && vOptional.get().getAuthor().getEmail().equals(userEmail)) {
            Review v = vOptional.get();
            v.setTitle(title);
            v.setScore(score);
            v.setComment(comment);
            reviewRepository.save(v);
            return true;
        }
        return false;
    }

    // FIX BAC (Broken Access Control): Added validation so ADMIN can also delete reviews.
    public boolean deleteReview(Long id, String userEmail) {
        Optional<Review> vOptional = reviewRepository.findById(id);
        if (vOptional.isPresent()) {
            User user = userService.findByEmail(userEmail).orElseThrow();
            boolean isOwner = vOptional.get().getAuthor().getEmail().equals(userEmail);
            boolean isAdmin = user.getRoles().contains("ADMIN");
            
            if (isOwner || isAdmin) {
                reviewRepository.deleteById(id);
                return true;
            }
        }
        return false;
    }
}