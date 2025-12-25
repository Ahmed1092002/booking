package com.example.booking.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponseDto {
    private Long id;
    private String name;
    private BigDecimal pricePerNight;
    private Integer capacity;
    private String viewType;
    private Boolean hasKitchen;
    private Boolean isAvailable;
    private Long hotelId;
    private String hotelName;
}
