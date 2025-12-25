package com.example.booking.review;

import com.example.booking.review.dto.ReviewResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponseDto toResponseDto(Review review) {
        if (review == null) {
            return null;
        }

        ReviewResponseDto dto = new ReviewResponseDto();
        dto.setId(review.getId());
        dto.setHotelId(review.getHotel().getId());
        dto.setHotelName(review.getHotel().getName());
        dto.setReviewerId(review.getReviewer().getId());
        dto.setReviewerName(review.getReviewer().getFullName());
        dto.setBookingId(review.getBooking().getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setSellerResponse(review.getSellerResponse());
        dto.setResponseDate(review.getResponseDate());

        return dto;
    }
}
