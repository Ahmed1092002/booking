package com.example.booking.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {
    private Long id;
    private Long hotelId;
    private String hotelName;
    private Long reviewerId;
    private String reviewerName;
    private Long bookingId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private String sellerResponse;
    private LocalDateTime responseDate;
}
