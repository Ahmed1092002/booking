package com.example.booking.review;

import com.example.booking.review.dto.CreateReviewRequest;
import com.example.booking.review.dto.ReviewResponseDto;
import com.example.booking.review.dto.SellerResponseRequest;
import com.example.booking.security.CurrentUserService;
import com.example.booking.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewMapper reviewMapper;
    private final CurrentUserService currentUserService;

    public ReviewController(ReviewService reviewService,
            ReviewMapper reviewMapper,
            CurrentUserService currentUserService) {
        this.reviewService = reviewService;
        this.reviewMapper = reviewMapper;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponseDto> createReview(@Valid @RequestBody CreateReviewRequest request) {
        User reviewer = currentUserService.getCurrentUser();
        Review review = reviewService.createReview(reviewer, request);
        return new ResponseEntity<>(reviewMapper.toResponseDto(review), HttpStatus.CREATED);
    }

    @PutMapping("/{reviewId}/respond")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ReviewResponseDto> respondToReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody SellerResponseRequest request) {
        User seller = currentUserService.getCurrentUser();
        Review review = reviewService.addSellerResponse(seller, reviewId, request.getResponse());
        return ResponseEntity.ok(reviewMapper.toResponseDto(review));
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<ReviewResponseDto>> getHotelReviews(@PathVariable Long hotelId) {
        List<Review> reviews = reviewService.getHotelReviews(hotelId);
        List<ReviewResponseDto> dtos = reviews.stream()
                .map(reviewMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/my-reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReviewResponseDto>> getMyReviews() {
        User user = currentUserService.getCurrentUser();
        List<Review> reviews = reviewService.getMyReviews(user.getId());
        List<ReviewResponseDto> dtos = reviews.stream()
                .map(reviewMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        User user = currentUserService.getCurrentUser();
        reviewService.deleteReview(user, reviewId);
        return ResponseEntity.noContent().build();
    }
}
