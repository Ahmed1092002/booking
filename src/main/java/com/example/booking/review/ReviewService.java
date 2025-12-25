package com.example.booking.review;

import java.math.BigDecimal;

import com.example.booking.booking.Booking;
import com.example.booking.booking.BookingRepository;
import com.example.booking.booking.BookingStatus;
import com.example.booking.exception.BadRequestException;
import com.example.booking.exception.ForbiddenException;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.hotel.Hotel;
import com.example.booking.hotel.HotelRepository;
import com.example.booking.review.dto.CreateReviewRequest;
import com.example.booking.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;

    public ReviewService(ReviewRepository reviewRepository,
            BookingRepository bookingRepository,
            HotelRepository hotelRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.hotelRepository = hotelRepository;
    }

    @Transactional
    public Review createReview(User reviewer, CreateReviewRequest request) {
        // Validate booking exists
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Validate user owns the booking
        if (!booking.getBooker().getId().equals(reviewer.getId())) {
            throw new ForbiddenException("You can only review your own bookings");
        }

        // Validate booking is completed (check-out date has passed)
        if (booking.getCheckOutDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("You can only review after checkout");
        }

        // Validate booking status
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Cannot review a cancelled booking");
        }

        // Validate no existing review for this booking
        if (reviewRepository.existsByBookingId(request.getBookingId())) {
            throw new BadRequestException("You have already reviewed this booking");
        }

        // Create review
        Review review = Review.builder()
                .hotel(booking.getRoom().getHotel())
                .reviewer(reviewer)
                .booking(booking)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);

        // Update hotel's average rating
        updateHotelRating(booking.getRoom().getHotel().getId());

        return savedReview;
    }

    @Transactional
    public Review addSellerResponse(User seller, Long reviewId, String response) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Validate seller owns the hotel
        if (!review.getHotel().getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("You can only respond to reviews for your hotels");
        }

        // Validate no existing response
        if (review.getSellerResponse() != null) {
            throw new BadRequestException("You have already responded to this review");
        }

        review.setSellerResponse(response);
        review.setResponseDate(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<Review> getHotelReviews(Long hotelId) {
        return reviewRepository.findByHotelIdOrderByCreatedAtDesc(hotelId);
    }

    @Transactional(readOnly = true)
    public List<Review> getMyReviews(Long userId) {
        return reviewRepository.findByReviewerIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void deleteReview(User user, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Only reviewer can delete their own review
        if (!review.getReviewer().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only delete your own reviews");
        }

        Long hotelId = review.getHotel().getId();
        reviewRepository.delete(review);

        // Update hotel's average rating
        updateHotelRating(hotelId);
    }

    private void updateHotelRating(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        Double averageRating = reviewRepository.calculateAverageRating(hotelId);
        Long totalReviews = reviewRepository.countByHotelId(hotelId);

        hotel.setAverageRating(averageRating != null ? BigDecimal.valueOf(averageRating) : BigDecimal.ZERO);
        hotel.setTotalReviews(totalReviews != null ? totalReviews.intValue() : 0);

        hotelRepository.save(hotel);
    }
}
