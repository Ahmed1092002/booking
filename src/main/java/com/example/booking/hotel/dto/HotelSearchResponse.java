package com.example.booking.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelSearchResponse {
    private Long id;
    private String name;
    private String city;
    private String address;
    private String googleMapUrl;
    private Set<String> amenities;
    private Long sellerId;
    private String sellerName;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private BigDecimal minRoomPrice;
    private Integer availableRooms;
    private List<RoomResponseDto> rooms;
}
