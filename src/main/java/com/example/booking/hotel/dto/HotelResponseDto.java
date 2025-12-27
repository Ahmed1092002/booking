package com.example.booking.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelResponseDto {
    private Long id;
    private String name;
    private String city;
    private String address;
    private String googleMapUrl;
    private Set<String> amenities;
    private Long sellerId;
    private String sellerName;
    private java.util.List<com.example.booking.image.dto.ImageResponseDto> images;
}
