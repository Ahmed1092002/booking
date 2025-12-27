package com.example.booking.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerStatsResponseDto {
    private int totalHotels;
    private int totalBookings;
    private BigDecimal totalRevenue;
    private BigDecimal averageRating;
    private int totalReviews;
}
