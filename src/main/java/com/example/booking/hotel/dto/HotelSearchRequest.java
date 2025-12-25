package com.example.booking.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelSearchRequest {
    private String city;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Set<String> amenities;
    private Integer minCapacity;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String sortBy; // price, rating, name
    private String sortOrder; // asc, desc
}
