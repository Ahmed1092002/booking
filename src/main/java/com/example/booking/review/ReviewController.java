package com.example.booking.review;

import com.example.booking.review.dto.CreateReviewRequest;
import com.example.booking.review.dto.ReviewResponseDto;
import com.example.booking.review.dto.SellerResponseRequest;
import com.example.booking.security.CurrentUserService;
import com.example.booking.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Hotel review and rating management endpoints")
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
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create hotel review", description = "Create a review for a completed booking. User must have completed the booking and cannot review the same booking twice.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Review created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviewResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request - Booking not completed or already reviewed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not own the booking"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<ReviewResponseDto> createReview(@Valid @RequestBody CreateReviewRequest request) {
        User reviewer = currentUserService.getCurrentUser();
        Review review = reviewService.createReview(reviewer, request);
        return new ResponseEntity<>(reviewMapper.toResponseDto(review), HttpStatus.CREATED);
    }

    @PutMapping("/{reviewId}/respond")
    @PreAuthorize("hasRole('SELLER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Respond to review", description = "Hotel owner can respond to a review. Only one response is allowed per review.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Response added successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviewResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Review already has a response"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the hotel owner"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<ReviewResponseDto> respondToReview(
            @Parameter(description = "Review ID", required = true) @PathVariable Long reviewId,
            @Valid @RequestBody SellerResponseRequest request) {
        User seller = currentUserService.getCurrentUser();
        Review review = reviewService.addSellerResponse(seller, reviewId, request.getResponse());
        return ResponseEntity.ok(reviewMapper.toResponseDto(review));
    }

    @GetMapping("/hotel/{hotelId}")
    @Operation(summary = "Get hotel reviews", description = "Retrieve all reviews for a specific hotel, ordered by creation date (newest first)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviewResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Hotel not found")
    })
    public ResponseEntity<List<ReviewResponseDto>> getHotelReviews(
            @Parameter(description = "Hotel ID", required = true) @PathVariable Long hotelId) {
        List<Review> reviews = reviewService.getHotelReviews(hotelId);
        List<ReviewResponseDto> dtos = reviews.stream()
                .map(reviewMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/my-reviews")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my reviews", description = "Retrieve all reviews written by the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviewResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
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
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete review", description = "Delete a review. Only the review author can delete their own review.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Review deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not own the review"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "Review ID", required = true) @PathVariable Long reviewId) {
        User user = currentUserService.getCurrentUser();
        reviewService.deleteReview(user, reviewId);
        return ResponseEntity.noContent().build();
    }
}
